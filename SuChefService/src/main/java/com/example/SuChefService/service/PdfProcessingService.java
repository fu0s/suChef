package com.example.SuChefService.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class PdfProcessingService {

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    @Value("${app.ocr.max-pdf-pages:500}")
    private int maxPdfPages;

    /**
     * Extracts text content from a PDF file.
     *
     * @param documentId The document ID (used as filename prefix)
     * @param originalFilename The original filename
     * @return Extracted text content from the PDF
     * @throws IOException If file reading fails
     */
    public String extractTextFromPdf(String documentId, String originalFilename) throws IOException {
        try {
            String storedFilename = documentId + ".pdf";
            Path filePath = Paths.get(uploadDir).resolve(storedFilename);

            if (!Files.exists(filePath)) {
                log.warn("PDF file not found at path: {}", filePath);
                return "";
            }

            StringBuilder extractedText = new StringBuilder();

            try (PDDocument document = PDDocument.load(filePath.toFile())) {
                // Check page count
                int pageCount = document.getNumberOfPages();
                if (pageCount > maxPdfPages) {
                    log.warn("PDF has {} pages, exceeds max limit of {}. Processing first {} pages.",
                            pageCount, maxPdfPages, maxPdfPages);
                }

                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setStartPage(1);
                textStripper.setEndPage(Math.min(pageCount, maxPdfPages));

                String text = textStripper.getText(document);
                extractedText.append(text);

                log.info("Successfully extracted text from PDF: {} ({} pages processed)",
                        originalFilename, Math.min(pageCount, maxPdfPages));
            }

            return extractedText.toString();

        } catch (IOException e) {
            log.error("Error extracting text from PDF {}: {}", originalFilename, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extracts text from specific pages in a PDF file.
     *
     * @param documentId The document ID (used as filename prefix)
     * @param originalFilename The original filename
     * @param startPage Starting page number (1-indexed)
     * @param endPage Ending page number (1-indexed)
     * @return Extracted text content from the specified pages
     * @throws IOException If file reading fails
     */
    public String extractTextFromPdfPages(String documentId, String originalFilename,
                                          int startPage, int endPage) throws IOException {
        try {
            String storedFilename = documentId + ".pdf";
            Path filePath = Paths.get(uploadDir).resolve(storedFilename);

            if (!Files.exists(filePath)) {
                log.warn("PDF file not found at path: {}", filePath);
                return "";
            }

            try (PDDocument document = PDDocument.load(filePath.toFile())) {
                int pageCount = document.getNumberOfPages();
                if (startPage < 1 || endPage > pageCount || startPage > endPage) {
                    log.warn("Invalid page range: {} - {} for document with {} pages",
                            startPage, endPage, pageCount);
                    return "";
                }

                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setStartPage(startPage);
                textStripper.setEndPage(endPage);

                return textStripper.getText(document);
            }

        } catch (IOException e) {
            log.error("Error extracting text from PDF pages {}: {}", originalFilename, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets the total page count of a PDF document.
     *
     * @param documentId The document ID (used as filename prefix)
     * @param originalFilename The original filename
     * @return Total number of pages in the PDF
     * @throws IOException If file reading fails
     */
    public int getPdfPageCount(String documentId, String originalFilename) throws IOException {
        try {
            String storedFilename = documentId + ".pdf";
            Path filePath = Paths.get(uploadDir).resolve(storedFilename);

            if (!Files.exists(filePath)) {
                log.warn("PDF file not found at path: {}", filePath);
                return 0;
            }

            try (PDDocument document = PDDocument.load(filePath.toFile())) {
                return document.getNumberOfPages();
            }

        } catch (IOException e) {
            log.error("Error getting page count for PDF {}: {}", originalFilename, e.getMessage(), e);
            throw e;
        }
    }
}
