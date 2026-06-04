package com.example.SuChefService.repository;

import com.example.SuChefService.entity.StockTransaction;
import com.example.SuChefService.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, String> {
    List<StockTransaction> findByItem(InventoryItem item);
}
