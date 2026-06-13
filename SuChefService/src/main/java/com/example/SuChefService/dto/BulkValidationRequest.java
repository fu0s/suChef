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
public class BulkValidationRequest {
    private List<String> documentIds;
    private String action; // "APPROVE_ALL", "REJECT_ALL"
    private Map<String, List<EditableItem>> editsPerDocument; // Optional edits for specific documents
}