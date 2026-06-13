package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkValidationResult {
    private boolean success;
    private String message;
    private int processedCount;
    private int failedCount;
    private Map<String, String> errors; // documentId -> error message
}