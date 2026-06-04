package com.example.SuChefService.repository;

import com.example.SuChefService.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, String> {
    Optional<Restaurant> findByName(String name);
}
