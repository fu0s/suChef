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
public class IngredientInput {
    private String inventoryItemId;
    private String inventoryItemName;
    private BigDecimal quantity;
    private String unit;
    private boolean isNewInventoryItem;
}