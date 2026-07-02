package com.example.SuChefService.entity;

import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "menu_item_ingredients")
public class MenuItemIngredient {

    @Id
    private String id;

    @NotBlank(message = "Menu item ID cannot be blank")
    @Size(min = 1, max = 36, message = "Menu item ID must be between 1 and 36 characters")
    private String menuItemId;

    @NotBlank(message = "Ingredient name cannot be blank")
    @Size(max = 100, message = "Ingredient name cannot exceed 100 characters")
    private String ingredientName;

    @NotBlank(message = "Quantity cannot be blank")
    private Integer quantity;

    @Size(max = 50, message = "Unit cannot exceed 50 characters")
    private String unit;

    public MenuItemIngredient() {
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