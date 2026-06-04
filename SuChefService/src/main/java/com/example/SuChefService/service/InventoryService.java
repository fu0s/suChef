package com.example.SuChefService.service;

import com.example.SuChefService.entity.*;
import com.example.SuChefService.repository.InventoryItemRepository;
import com.example.SuChefService.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockTransactionRepository stockTransactionRepository;

    @Transactional
    @SuppressWarnings("null")
    public void updateStock(String itemName, BigDecimal quantityChange, StockTransactionType type, Document document,
            User user) {
        InventoryItem item = inventoryItemRepository.findByUser(user).stream()
                .filter(i -> i.getName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElseGet(() -> {
                    InventoryItem newItem = InventoryItem.builder()
                            .id(UUID.randomUUID().toString())
                            .name(itemName)
                            .currentStock(BigDecimal.ZERO)
                            .user(user)
                            .build();
                    return inventoryItemRepository.save(newItem);
                });

        item.setCurrentStock(item.getCurrentStock().add(quantityChange));
        inventoryItemRepository.save(item);

        StockTransaction transaction = StockTransaction.builder()
                .id(UUID.randomUUID().toString())
                .item(item)
                .quantityChange(quantityChange)
                .type(type)
                .document(document)
                .timestamp(LocalDateTime.now())
                .build();
        stockTransactionRepository.save(transaction);
    }
}
