package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {
    private String action; // "APPROVE", "REJECT", "EDIT"
    private List<EditableItem> editedItems; // For EDIT and APPROVE with edits
}