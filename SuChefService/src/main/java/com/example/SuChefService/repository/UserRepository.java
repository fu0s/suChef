package com.example.SuChefService.repository;

import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    long countByRestaurant(Restaurant restaurant);
}
