package com.example.SuChefService.service;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.bytedeco.leptonica.global.leptonica.pixDestroy;
import static org.bytedeco.leptonica.global.leptonica.pixRead;

@Service
@Slf4j
public class ImageProcessingService {

    private static final List<String> SUPPORTED_IMAGE_EXTENSIONS = Arrays.asList(
            ".png", ".jpg", ".jpeg", ".tiff", ".tif", ".gif", ".bmp", ".webp"
    );

    private static final long MAX_IMAGE_SIZE = 50 * 1024 * 1024; // 50 MB

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    @Value("${app.ocr.confidence-threshold:0.5}")
    private double confidenceThreshold;

    // NOTE: If using Bytedeco, this path must point to the PARENT folder containing "tessdata"
    // or be null to use default system paths.
    @Value("${app.ocr.tesseract-data-path:/opt/homebrew/opt/tesseract/share}") 
    private String tesseractDataPath;

    /**
     * Helper to initialize the Tesseract API.
     * Unlike Tess4J, Bytedeco requires manual initialization check.
     */
    private TessBaseAPI initializeTesseract(String language) throws IOException {
        TessBaseAPI api = new TessBaseAPI();
        
        // Initialize tesseract-ocr with the specified language (default "eng")
        // The first argument is the datapath, second is language
        if (api.Init(tesseractDataPath, language) != 0) {
            api.close(); // Clean up if init fails
            throw new IOException("Could not initialize Tesseract with datapath: " + tesseractDataPath + " and language: " + language);
        }
        
        return api;
    }

    /**
     * Performs OCR on an image file to extract text content.
     */
    public String performOcr(String documentId, String originalFilename) throws IOException {
        return performOcrWithLanguage(documentId, originalFilename, "eng");
    }

    /**
     * Performs OCR with language specification.
     */
    public String performOcrWithLanguage(String documentId, String originalFilename, String language) throws IOException {
        // 1. File Validation Logic
        String extension = getFileExtension(originalFilename);
        String storedFilename = documentId + extension;
        Path filePath = Paths.get(uploadDir).resolve(storedFilename);

        if (!Files.exists(filePath)) {
            log.warn("Image file not found at path: {}", filePath);
            return "";
        }

        long fileSize = Files.size(filePath);
        if (fileSize > MAX_IMAGE_SIZE) {
            log.warn("Image file {} exceeds max size", originalFilename);
            return "";
        }

        if (!isValidImageFormat(extension)) {
            log.warn("Unsupported image format: {}", extension);
            return "";
        }

        log.info("Starting OCR for image: {} (size: {} bytes) with language: {}", originalFilename, fileSize, language);

        // 2. OCR Processing using Bytedeco
        // We use try-with-resources for TessBaseAPI to ensure C++ memory is freed
        try (TessBaseAPI api = initializeTesseract(language)) {
            
            // Open input image with Leptonica library
            // pixRead loads the image into memory (C++ pointer)
            PIX image = pixRead(filePath.toString());
            
            if (image == null) {
                throw new IOException("Failed to read image file using Leptonica: " + filePath);
            }

            try {
                api.SetImage(image);
                
                // Get OCR result
                // BytePointer prevents memory leaks compared to raw String return in some versions
                BytePointer outText = api.GetUTF8Text();
                
                if (outText == null) {
                    log.warn("No text extracted from image: {}", originalFilename);
                    return "";
                }

                String extractedText = outText.getString();
                
                // Manually deallocate the pointer text resource
                outText.deallocate();

                log.info("Successfully completed OCR for image: {} (extracted {} characters)",
                        originalFilename, extractedText.length());

                return extractedText;

            } finally {
                // Crucial: Destroy the image pointer to prevent memory leaks
                pixDestroy(image);
            }
        } catch (Exception e) {
            log.error("OCR error for {}: {}", originalFilename, e.getMessage(), e);
            throw new IOException("OCR processing failed", e);
        }
    }

    public boolean isValidImageFormat(String extension) {
        return SUPPORTED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public List<String> getSupportedExtensions() {
        return SUPPORTED_IMAGE_EXTENSIONS;
    }
}