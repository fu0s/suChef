package com.example.SuChefService.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddMenuItemRequest {
    @NotBlank(message = "Menu item name cannot be blank")
    @Size(max = 100, message = "Menu item name cannot exceed 100 characters")
    private String name;
    private Double price;
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;
    @NotBlank(message = "Restaurant ID cannot be blank")
    private String restaurantId;

    private List<MenuItemIngredientDto> ingredients;

    public AddMenuItemRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<MenuItemIngredientDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<MenuItemIngredientDto> ingredients) {
        this.ingredients = ingredients;
    }
}