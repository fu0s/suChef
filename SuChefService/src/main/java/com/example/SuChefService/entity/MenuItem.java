package com.example.SuChefService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}
