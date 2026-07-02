package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummary {
    private String id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemSummary> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class OrderItemSummary {
    private String menuItemName;
    private Integer quantity;
    private BigDecimal price;
}