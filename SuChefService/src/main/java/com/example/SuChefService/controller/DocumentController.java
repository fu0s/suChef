package com.example.SuChefService.controller;

import com.example.SuChefService.dto.DocumentResponse;
import com.example.SuChefService.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(documentService.uploadDocument(file));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getUserDocuments() {
        return ResponseEntity.ok(documentService.getUserDocuments());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) throws IOException {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocument(@PathVariable String id)
            throws IOException {
        return documentService.downloadDocument(id);
    }
}
