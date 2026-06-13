package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemSummary {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private List<IngredientSummary> ingredients;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class IngredientSummary {
    private String inventoryItemId;
    private String inventoryItemName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal minThreshold;
    private boolean lowStock;
}