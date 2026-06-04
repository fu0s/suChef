package com.example.SuChefService.repository;

import com.example.SuChefService.entity.InventoryItem;
import com.example.SuChefService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
    List<InventoryItem> findByUser(User user);

    @Query("SELECT i FROM InventoryItem i WHERE i.user = :user AND i.currentStock < i.minThreshold")
    List<InventoryItem> findLowStockByUser(@Param("user") User user);

    List<InventoryItem> findByUserAndCategory(User user, String category);
}
