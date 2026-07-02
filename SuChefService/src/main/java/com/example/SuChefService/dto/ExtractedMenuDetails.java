package com.example.SuChefService.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ExtractedMenuDetails {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private ExtractedItem[] items;

    public ExtractedMenuDetails() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExtractedItem[] getItems() {
        return items;
    }

    public void setItems(ExtractedItem[] items) {
        this.items = items;
    }
}