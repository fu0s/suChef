package com.example.SuChefService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    @Id
    private String id;
    private String name;

    @Column(name = "current_stock")
    private BigDecimal currentStock;
    private String unit;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "min_threshold")
    private BigDecimal minThreshold;
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
