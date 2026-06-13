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
public class RecipeViewSummary {
    private String menuItemId;
    private String menuItemName;
    private String description;
    private BigDecimal price;
    private String category;
    private List<RecipeIngredientView> ingredients;
    private BigDecimal totalIngredientCost;
    private BigDecimal profitMargin;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class RecipeIngredientView {
    private String inventoryItemId;
    private String inventoryItemName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal currentStock;
    private BigDecimal minThreshold;
    private boolean lowStock;
    private BigDecimal lineCost;
}