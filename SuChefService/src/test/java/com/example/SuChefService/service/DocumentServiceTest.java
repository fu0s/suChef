package com.example.SuChefService.service;

import com.example.SuChefService.dto.DocumentResponse;
import com.example.SuChefService.entity.*;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.exception.UnauthorizedException;
import com.example.SuChefService.repository.DocumentRepository;
import com.example.SuChefService.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentAnalysisService documentAnalysisService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private DocumentService documentService;

    private User currentUser;
    private Restaurant currentRestaurant;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(documentService, "uploadDir", "uploads/documents");

        currentRestaurant = Restaurant.builder()
                .id("rest-123")
                .name("Test Restaurant")
                .build();

        currentUser = User.builder()
                .id("user-123")
                .name("John Doe")
                .email("test@example.com")
                .restaurant(currentRestaurant)
                .build();

        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn("test@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadDocument_success_shouldSaveAndAnalyze() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("invoice.pdf");
        when(file.getSize()).thenReturn(2048L);

        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            Path mockTempFile = mock(Path.class);
            File mockFile = mock(File.class);
            when(mockTempFile.toFile()).thenReturn(mockFile);

            filesMockedStatic.when(() -> Files.createTempFile(anyString(), anyString())).thenReturn(mockTempFile);
            filesMockedStatic.when(() -> Files.probeContentType(mockTempFile)).thenReturn("application/pdf");
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.deleteIfExists(mockTempFile)).thenReturn(true);

            doNothing().when(file).transferTo(mockFile);

            Document savedDoc = Document.builder()
                    .id("doc-123")
                    .name("invoice.pdf")
                    .type("pdf")
                    .size(2048L)
                    .status(DocumentStatus.RECEIVED)
                    .classification(DocumentClassification.BILL)
                    .user(currentUser)
                    .build();

            when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

            DocumentResponse response = documentService.uploadDocument(file);

            assertNotNull(response);
            assertEquals("doc-123", response.getId());
            assertEquals("invoice.pdf", response.getName());
            assertEquals("pdf", response.getType());
            assertEquals(2048L, response.getSize());

            verify(subscriptionService).checkDocumentLimit(currentRestaurant, 2048L);
            verify(subscriptionService).incrementDocumentUsage(currentRestaurant, 2048L);
            verify(documentAnalysisService).analyzeDocument("doc-123");
            filesMockedStatic.verify(() -> Files.deleteIfExists(mockTempFile), times(1));
        }
    }

    @Test
    void uploadDocument_invalidExtension_shouldThrowIllegalArgumentException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("malicious.exe");

        assertThrows(IllegalArgumentException.class, () -> documentService.uploadDocument(file));

        verifyNoInteractions(subscriptionService, documentAnalysisService, documentRepository);
    }

    @Test
    void uploadDocument_invalidMimeType_shouldThrowIllegalArgumentExceptionAndCleanUpTempFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("invoice.pdf");
        when(file.getSize()).thenReturn(2048L);

        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            Path mockTempFile = mock(Path.class);
            File mockFile = mock(File.class);
            when(mockTempFile.toFile()).thenReturn(mockFile);

            filesMockedStatic.when(() -> Files.createTempFile(anyString(), anyString())).thenReturn(mockTempFile);
            filesMockedStatic.when(() -> Files.probeContentType(mockTempFile)).thenReturn("application/octet-stream");
            filesMockedStatic.when(() -> Files.deleteIfExists(mockTempFile)).thenReturn(true);

            doNothing().when(file).transferTo(mockFile);

            assertThrows(IllegalArgumentException.class, () -> documentService.uploadDocument(file));

            verifyNoInteractions(subscriptionService, documentAnalysisService, documentRepository);
            filesMockedStatic.verify(() -> Files.deleteIfExists(mockTempFile), times(1));
        }
    }

    @Test
    void getUserDocuments_shouldReturnMappedList() {
        LocalDateTime now = LocalDateTime.now();
        Document doc = Document.builder()
                .id("doc-123")
                .name("invoice.pdf")
                .type("pdf")
                .size(2048L)
                .date(now)
                .uploadedAt(now)
                .status(DocumentStatus.RECEIVED)
                .classification(DocumentClassification.BILL)
                .user(currentUser)
                .build();
        when(documentRepository.findByUser(currentUser)).thenReturn(Collections.singletonList(doc));

        List<DocumentResponse> responses = documentService.getUserDocuments();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        DocumentResponse response = responses.get(0);
        assertEquals("doc-123", response.getId());
        assertEquals("invoice.pdf", response.getName());
        assertEquals("BILL", response.getClassification());
    }

    @Test
    void deleteDocument_success_shouldDeleteFileAndRecord() throws Exception {
        Document document = Document.builder()
                .id("doc-123")
                .name("invoice.pdf")
                .user(currentUser)
                .build();

        when(documentRepository.findById("doc-123")).thenReturn(Optional.of(document));

        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            documentService.deleteDocument("doc-123");

            verify(documentRepository).delete(document);
            filesMockedStatic.verify(() -> Files.deleteIfExists(any(Path.class)), times(1));
        }
    }

    @Test
    void deleteDocument_unauthorizedUser_shouldThrowUnauthorizedException() throws Exception {
        User otherUser = User.builder().id("other-user").email("other@example.com").build();
        Document document = Document.builder()
                .id("doc-123")
                .name("invoice.pdf")
                .user(otherUser)
                .build();

        when(documentRepository.findById("doc-123")).thenReturn(Optional.of(document));

        assertThrows(UnauthorizedException.class, () -> documentService.deleteDocument("doc-123"));

        verify(documentRepository, never()).delete(any(Document.class));
    }

    @Test
    void downloadDocument_success_shouldReturnResource() throws Exception {
        Document document = Document.builder()
                .id("doc-123")
                .name("invoice.pdf")
                .user(currentUser)
                .build();

        when(documentRepository.findById("doc-123")).thenReturn(Optional.of(document));

        ResponseEntity<Resource> response = documentService.downloadDocument("doc-123");

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("attachment; filename=\"invoice.pdf\"", response.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    void downloadDocument_unauthorizedUser_shouldThrowUnauthorizedException() {
        User otherUser = User.builder().id("other-user").email("other@example.com").build();
        Document document = Document.builder()
                .id("doc-123")
                .name("invoice.pdf")
                .user(otherUser)
                .build();

        when(documentRepository.findById("doc-123")).thenReturn(Optional.of(document));

        assertThrows(UnauthorizedException.class, () -> documentService.downloadDocument("doc-123"));
    }
}
