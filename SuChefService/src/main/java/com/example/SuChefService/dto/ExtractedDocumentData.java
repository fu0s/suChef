package com.example.SuChefService.dto;

import com.example.SuChefService.entity.DocumentClassification;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExtractedDocumentData {
    @JsonPropertyDescription("The type of document: BILL, ORDER, or MENU")
    private DocumentClassification classification;
    private List<ExtractedItem> items;
    private String vendorName;
    @JsonPropertyDescription("The final total price including tax. Use a plain number, no symbols.")
    private BigDecimal totalAmount;
    private String restaurantName;

    @Data
    public static class ExtractedItem {
        private String name;
        private BigDecimal quantity;
        private BigDecimal price;
        private String category;
    }
}
