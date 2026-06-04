package com.example.SuChefService.repository;

import com.example.SuChefService.entity.RestaurantMetric;
import com.example.SuChefService.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RestaurantMetricRepository extends JpaRepository<RestaurantMetric, String> {
    List<RestaurantMetric> findByRestaurant(Restaurant restaurant);
}
