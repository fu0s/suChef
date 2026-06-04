package com.example.SuChefService.service;

import com.example.SuChefService.dto.AuthRequest;
import com.example.SuChefService.dto.AuthResponse;
import com.example.SuChefService.dto.RegisterRequest;
import com.example.SuChefService.entity.User;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.repository.UserRepository;
import com.example.SuChefService.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.repository.RestaurantRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final RestaurantRepository restaurantRepository;
        private final SubscriptionService subscriptionService;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                String reqRestName = request.getRestaurantName();
                String restaurantName = (reqRestName != null && !reqRestName.trim().isEmpty()) ? reqRestName
                                : "My Restaurant";

                // Find existing restaurant by name or create a new one
                Restaurant restaurant = restaurantRepository.findByName(restaurantName)
                                .orElseGet(() -> {
                                        Restaurant newRestaurant = Restaurant.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .name(restaurantName)
                                                        .build();
                                        return restaurantRepository.save(newRestaurant);
                                });

                // Check account limit
                subscriptionService.checkAccountLimit(restaurant);

                var user = User.builder()
                                .id(UUID.randomUUID().toString())
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .restaurant(restaurant)
                                .build();
                repository.save(user);
                var jwtToken = jwtUtil.generateToken(user.getEmail());
                return AuthResponse.builder()
                                .token(jwtToken)
                                .name(user.getName())
                                .email(user.getEmail())
                                .restaurantName(user.getRestaurant() != null ? user.getRestaurant().getName() : null)
                                .build();
        }

        @Transactional(readOnly = true)
        public AuthResponse authenticate(AuthRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));
                var user = repository.findByEmail(request.getEmail())
                                .orElseThrow();
                var jwtToken = jwtUtil.generateToken(user.getEmail());
                return AuthResponse.builder()
                                .token(jwtToken)
                                .name(user.getName())
                                .email(user.getEmail())
                                .restaurantName(user.getRestaurant() != null ? user.getRestaurant().getName() : null)
                                .build();
        }

        @Transactional
        public AuthResponse setRestaurant(String email, String restaurantName) {
                User user = repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
                Restaurant restaurant = restaurantRepository.findByName(restaurantName)
                                .orElseGet(() -> {
                                        Restaurant newRestaurant = Restaurant.builder()
                                                        .id(UUID.randomUUID().toString())
                                                        .name(restaurantName)
                                                        .build();
                                        return restaurantRepository.save(newRestaurant);
                                });
                
                user.setRestaurant(restaurant);
                repository.save(user);

                var jwtToken = jwtUtil.generateToken(user.getEmail());
                return AuthResponse.builder()
                        .token(jwtToken)
                        .name(user.getName())
                        .email(user.getEmail())
                        .restaurantName(restaurant.getName())
                        .build();
        }
}
