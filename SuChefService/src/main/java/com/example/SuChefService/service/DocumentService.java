package com.example.SuChefService.service;

import com.example.SuChefService.dto.DocumentResponse;
import com.example.SuChefService.entity.Document;
import com.example.SuChefService.entity.DocumentStatus;
import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.User;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.exception.UnauthorizedException;
import com.example.SuChefService.mcp.McpToolProvider;

import com.example.SuChefService.repository.DocumentRepository;
import com.example.SuChefService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentAnalysisService documentAnalysisService;
    private final SubscriptionService subscriptionService;
    private final McpToolProvider mcpToolProvider;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf", ".png", ".jpg", ".jpeg"
    );

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @SuppressWarnings("null")
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file) throws IOException {
        User user = getCurrentUser();
        Restaurant restaurant = user.getRestaurant();
        long fileSize = file.getSize();

        // Validate file extension before any filesystem operations
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "File type not allowed: " + extension + ". Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Probe actual content type from file bytes (not spoofable via request header)
        Path tempFile = Files.createTempFile("upload-validate-", extension);
        try {
            file.transferTo(tempFile.toFile());
            String probedType = Files.probeContentType(tempFile);
            if (probedType == null || !ALLOWED_MIME_TYPES.contains(probedType)) {
                throw new IllegalArgumentException(
                        "File content type not allowed: " + probedType + ". Allowed types: " + ALLOWED_MIME_TYPES);
            }

            // Check subscription limits
            subscriptionService.checkDocumentLimit(restaurant, fileSize);

            String fileId = UUID.randomUUID().toString();
            String storedFilename = fileId + extension;

            Path rootPath = Paths.get(uploadDir);
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }

            Path targetLocation = rootPath.resolve(storedFilename);
            Files.copy(tempFile, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Document document = Document.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .type(extension.replace(".", ""))
                    .size(fileSize)
                    .date(LocalDateTime.now())
                    .uploadedAt(LocalDateTime.now())
                    .status(DocumentStatus.RECEIVED)
                    .user(user)
                    .build();

            Document savedDocument = documentRepository.save(document);

            // Track usage
            subscriptionService.incrementDocumentUsage(restaurant, fileSize);

            documentAnalysisService.analyzeDocument(savedDocument.getId());
            return toDocumentResponse(savedDocument);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    public List<DocumentResponse> getUserDocuments() {
        @SuppressWarnings("unchecked")
        List<McpToolProvider.DocumentInfo> docs =
                (List<McpToolProvider.DocumentInfo>) (List<?>) mcpToolProvider.getUserDocuments();
        return docs.stream()
                .map(d -> DocumentResponse.builder()
                        .id(d.id())
                        .name(d.name())
                        .type(d.type())
                        .size(d.size())
                        .date(d.date())
                        .uploadedAt(d.uploadedAt())
                        .status(d.status())
                        .classification(d.classification())
                        .build())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteDocument(String id) throws IOException {
        User user = getCurrentUser();
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized delete attempt");
        }

        // Delete file
        String originalFilename = document.getName();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = document.getId() + extension;
        Path filePath = Paths.get(uploadDir).resolve(storedFilename);
        Files.deleteIfExists(filePath);

        documentRepository.delete(document);
    }

    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadDocument(String id)
            throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        User user = getCurrentUser();
        if (!document.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized download attempt");
        }

        String originalFilename = document.getName();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = document.getId() + extension;
        Path filePath = Paths.get(uploadDir).resolve(storedFilename);

        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getName() + "\"")
                .body(resource);
    }

    private DocumentResponse toDocumentResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .name(doc.getName())
                .type(doc.getType())
                .size(doc.getSize())
                .date(doc.getDate())
                .uploadedAt(doc.getUploadedAt())
                .status(doc.getStatus() != null ? doc.getStatus().name() : null)
                .classification(doc.getClassification() != null ? doc.getClassification().name() : null)
                .build();
    }
}
