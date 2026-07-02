package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummary {
    private String id;
    private String name;
    private BigDecimal currentStock;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal minThreshold;
    private String category;
    private BigDecimal totalValue;
    private boolean lowStock;
}