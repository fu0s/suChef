package com.example.SuChefService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MenuItemIngredientDto {
    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(max = 100, message = "Ingredient name cannot exceed 100 characters")
    private String ingredientName;
    @NotBlank(message = "Quantity cannot be blank")
    private Integer quantity;
    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    private String unit;

    public MenuItemIngredientDto() {
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