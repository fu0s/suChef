package com.example.SuChefService.repository;

import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.SubscriptionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, String> {
    Optional<SubscriptionUsage> findByRestaurantAndMonthYear(Restaurant restaurant, String monthYear);
}
