package com.example.SuChefService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private InventoryItem item;

    @Column(name = "quantity_change")
    private BigDecimal quantityChange;

    @Enumerated(EnumType.STRING)
    private StockTransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    private LocalDateTime timestamp;
}
