package com.example.SuChefService.service;

import com.example.SuChefService.dto.ExtractedDocumentData;
import com.example.SuChefService.entity.*;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentAnalysisService {

    private final DocumentRepository documentRepository;
    private final RestaurantRepository restaurantRepository;
    private final VendorRepository vendorRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PdfProcessingService pdfProcessingService;
    private final ImageProcessingService imageProcessingService;
    private final ChatClient chatClient;

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    @Async
    @Transactional
    @SuppressWarnings("null")
    public void analyzeDocument(String documentId) {
        log.info("Starting analysis for document: {}", documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        document.setStatus(DocumentStatus.PENDING_VALIDATION);
        documentRepository.save(document);

        try {
            // Read document content with support for PDF, images, and text files
            String content = readDocumentContent(document);

            if (content == null || content.trim().isEmpty()) {
                log.warn("No content extracted from document: {}", documentId);
                document.setStatus(DocumentStatus.FAILED);
                documentRepository.save(document);
                return;
            }

            // AI Analysis
            String restaurantName = document.getUser() != null && document.getUser().getRestaurant() != null
                    ? document.getUser().getRestaurant().getName()
                    : "Unknown Restaurant";
            ExtractedDocumentData extractedData = analyzeWithAI(content, restaurantName);

            // Store Data Based on Classification
            processExtractedData(document, extractedData);

            document.setStatus(DocumentStatus.COMPLETED);
            document.setClassification(extractedData.getClassification());
            documentRepository.save(document);
            log.info("Completed analysis for document: {}", documentId);

        } catch (Exception e) {
            log.error("Error analyzing document {}: {}", documentId, e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
        }
    }

    @Async
    @Transactional
    @SuppressWarnings("null")
    public Document createPendingValidation(DocumentDetailsRequest request) {
        Objects.requireNonNull(request);

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setStatus(DocumentStatus.PENDING_VALIDATION);
        document.setClassification(request.getClassification());
        document.setDetails(request.getDetails());

        Document savedDocument = documentRepository.save(document);
        return savedDocument;
    }

    private String readDocumentContent(Document document) throws IOException {
        String originalFilename = document.getName();
        String extension = getFileExtension(originalFilename);
        String documentId = document.getId();

        log.info("Processing document: {} with extension: {}", originalFilename, extension);

        // Route to appropriate processor based on file type
        if (isPdfFile(extension)) {
            return readPdfContent(documentId, originalFilename);
        } else if (isImageFile(extension)) {
            return readImageContent(documentId, originalFilename);
        } else {
            return readTextContent(documentId, originalFilename, extension);
        }
    }

    /**
     * Extracts content from a PDF file.
     */
    private String readPdfContent(String documentId, String originalFilename) throws IOException {
        try {
            log.info("Extracting content from PDF: {}", originalFilename);
            String content = pdfProcessingService.extractTextFromPdf(documentId, originalFilename);

            if (content == null || content.trim().isEmpty()) {
                log.warn("No text extracted from PDF: {}", originalFilename);
                return "";
            }

            log.info("Successfully extracted {} characters from PDF: {}",
                    content.length(), originalFilename);
            return content;

        } catch (IOException e) {
            log.error("Failed to extract content from PDF {}: {}", originalFilename, e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts content from an image file using OCR.
     */
    private String readImageContent(String documentId, String originalFilename) throws IOException {
        try {
            log.info("Performing OCR on image: {}", originalFilename);
            String content = imageProcessingService.performOcr(documentId, originalFilename);

            if (content == null || content.trim().isEmpty()) {
                log.warn("No text extracted from image via OCR: {}", originalFilename);
                return "";
            }

            log.info("Successfully extracted {} characters from image: {}",
                    content.length(), originalFilename);
            return content;

        } catch (IOException e) {
            log.error("Failed to process image {}: {}", originalFilename, e.getMessage());
            throw e;
        }
    }

    /**
     * Reads content from a text file.
     */
    private String readTextContent(String documentId, String originalFilename, String extension) throws IOException {
        try {
            String storedFilename = documentId + extension;
            Path filePath = Paths.get(uploadDir).resolve(storedFilename);

            if (Files.exists(filePath)) {
                String content = new String(Files.readAllBytes(filePath));
                log.info("Successfully read text file: {}", originalFilename);
                return content;
            } else {
                log.warn("Text file not found at path: {}", filePath);
                return "";
            }

        } catch (IOException e) {
            log.error("Failed to read text file {}: {}", originalFilename, e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Checks if the file is a PDF.
     */
    private boolean isPdfFile(String extension) {
        return extension.equalsIgnoreCase(".pdf");
    }

    /**
     * Checks if the file is an image file.
     */
    private boolean isImageFile(String extension) {
        return imageProcessingService.isValidImageFormat(extension);
    }

    private ExtractedDocumentData analyzeWithAI(String content, String userRestaurantName) {
        var converter = new BeanOutputConverter<>(ExtractedDocumentData.class);

        return chatClient.prompt()
                .options(OllamaChatOptions.builder()
                        .format("json")
                        .temperature(0.1)
                        .build())
                .user(u -> u
                        .text("""
                                You are a data extraction expert.
                                Analyze the OCR text provided below and convert it into a structured JSON object.

                                STEPS:
                                1. Identify if the document is a BILL, ORDER, or MENU.
                                2. Extract all items, quantities, and prices.
                                3. Calculate the final total amount if not explicitly stated, or verify the stated total.

                                IMPORTANT CONTEXT:
                                The user submitting this document owns a restaurant named: "{userRestaurantName}".
                                Use this name to correctly identify if the document belongs to their restaurant or a vendor they do business with.

                                RULES:
                                - Output MUST be valid JSON.
                                - Do NOT include any explanations, math formulas, or markdown backticks.
                                - If a value is missing, use null or an empty string as appropriate.

                                {format}

                                DOCUMENT CONTENT:
                                {documentContent}
                                """)
                        .param("documentContent", content)
                        .param("userRestaurantName", userRestaurantName)
                        .param("format", converter.getFormat()))
                .call()
                .entity(ExtractedDocumentData.class);
    }

    @SuppressWarnings("null")
    private void processExtractedData(Document document, ExtractedDocumentData data) {
        User user = document.getUser();

        if (data.getClassification() == DocumentClassification.BILL) {
            // Handle Supplier Bill: Create Vendor if not exists, update inventory
            findOrCreateVendor(data.getVendorName(), user);
            for (ExtractedDocumentData.ExtractedItem item : data.getItems()) {
                inventoryService.updateStock(item.getName(), item.getQuantity(), StockTransactionType.PURCHASE,
                        document, user);
            }
        } else if (data.getClassification() == DocumentClassification.ORDER) {
            // Handle Customer Order: Update restaurant metrics, deduct inventory
            Restaurant restaurant = findOrCreateRestaurant(data.getRestaurantName(), user);
            Order order = Order.builder()
                    .id(UUID.randomUUID().toString())
                    .orderDate(LocalDateTime.now())
                    .totalAmount(data.getTotalAmount())
                    .status("COMPLETED")
                    .restaurant(restaurant)
                    .build();
            orderRepository.save(order);

            for (ExtractedDocumentData.ExtractedItem item : data.getItems()) {
                inventoryService.updateStock(item.getName(), item.getQuantity().negate(), StockTransactionType.SALE,
                        document, user);
            }
        } else if (data.getClassification() == DocumentClassification.MENU) {
            // Handle Menu: Update/Create Menu items
            Restaurant restaurant = findOrCreateRestaurant(data.getRestaurantName(), user);
            for (ExtractedDocumentData.ExtractedItem item : data.getItems()) {
                MenuItem menuItem = MenuItem.builder()
                        .id(UUID.randomUUID().toString())
                        .name(item.getName())
                        .price(item.getPrice())
                        .category(item.getCategory())
                        .restaurant(restaurant)
                        .build();
                menuItemRepository.save(menuItem);
            }
        }
    }

    @SuppressWarnings("null")
    private Vendor findOrCreateVendor(String name, User user) {
        String finalName = name != null && !name.trim().isEmpty() ? name : "Unknown Vendor";
        return vendorRepository.findByUser(user).stream()
                .filter(v -> v.getName() != null && v.getName().equalsIgnoreCase(finalName))
                .findFirst()
                .orElseGet(() -> {
                    Vendor vendor = Vendor.builder()
                            .id(UUID.randomUUID().toString())
                            .name(finalName)
                            .user(user)
                            .build();
                    return vendorRepository.save(vendor);
                });
    }

    @SuppressWarnings("null")
    private Restaurant findOrCreateRestaurant(String name, User user) {
        String finalName = name != null && !name.trim().isEmpty() ? name : "Default Restaurant";
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getName() != null && r.getName().equalsIgnoreCase(finalName))
                .findFirst()
                .orElseGet(() -> {
                    Restaurant restaurant = Restaurant.builder()
                            .id(UUID.randomUUID().toString())
                            .name(finalName)
                            .build();
                    return restaurantRepository.save(restaurant);
                });
    }
}
