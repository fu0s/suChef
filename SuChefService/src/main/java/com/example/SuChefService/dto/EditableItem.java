package com.example.SuChefService.dto;

import java.util.UUID;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EditableItem {

    private UUID id;
    @NotBlank(message = "Item name cannot be blank")
    @Size(max = 100, message = "Item name cannot exceed 100 characters")
    private String name;
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;
    private Double price;
    private Integer quantity;

    public EditableItem() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}