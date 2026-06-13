package com.example.SuChefService.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class IngredientOverviewDTO {
    private String id;
    private String menuItemId;
    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(max = 100, message = "Ingredient name cannot exceed 100 characters")
    private String ingredientName;
    private Integer quantity;
    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    public IngredientOverviewDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}