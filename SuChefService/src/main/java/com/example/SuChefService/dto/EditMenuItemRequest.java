package com.example.SuChefService.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EditMenuItemRequest {
    @NotBlank(message = "Menu item ID cannot be blank")
    private String id;
    @NotBlank(message = "Menu item name cannot be blank")
    @Size(max = 100, message = "Menu item name cannot exceed 100 characters")
    private String name;
    private Double price;
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    public EditMenuItemRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}