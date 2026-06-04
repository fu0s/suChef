package com.example.SuChefService.repository;

import com.example.SuChefService.entity.MenuItem;
import com.example.SuChefService.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);
    List<MenuItem> findByRestaurantAndCategory(Restaurant restaurant, String category);
}
