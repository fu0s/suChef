package com.example.SuChefService.dto;

import com.example.SuChefService.entity.DocumentClassification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingValidationEntry {
    private String documentId;
    private String documentName;
    private DocumentClassification classification;
    private List<EditableItem> items;
    private BigDecimal totalAmount;
    private String vendorName;
    private String restaurantName;
    private LocalDateTime uploadedAt;
}