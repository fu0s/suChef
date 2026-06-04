package com.example.SuChefService.repository;

import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.RestaurantSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantSubscriptionRepository extends JpaRepository<RestaurantSubscription, String> {
    Optional<RestaurantSubscription> findByRestaurant(Restaurant restaurant);
}
