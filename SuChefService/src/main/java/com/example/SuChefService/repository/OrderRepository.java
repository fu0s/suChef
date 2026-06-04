package com.example.SuChefService.repository;

import com.example.SuChefService.entity.Order;
import com.example.SuChefService.entity.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByRestaurant(Restaurant restaurant);

    @EntityGraph(attributePaths = {"items", "items.menuItem"})
    List<Order> findTop10ByRestaurantOrderByOrderDateDesc(Restaurant restaurant);

    @EntityGraph(attributePaths = {"items", "items.menuItem"})
    List<Order> findByRestaurantAndOrderDateBetween(Restaurant restaurant, LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"items", "items.menuItem"})
    List<Order> findByRestaurantAndStatus(Restaurant restaurant, String status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.items.menuItem WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") String id);
}
