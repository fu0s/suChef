package com.example.SuChefService.chat;

import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.User;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnlimitedChatUsagePolicy implements ChatUsagePolicy {

    private final UserRepository userRepository;

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    @Override
    public Restaurant getCurrentRestaurant() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (user.getRestaurant() == null) {
                        log.info("Creating default restaurant for demo user: {}", email);
                        var restaurant = new com.example.SuChefService.entity.Restaurant();
                        restaurant.setId(UUID.randomUUID().toString());
                        restaurant.setName("Demo Restaurant");
                        return restaurant;
                    }
                    return user.getRestaurant();
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void checkChatLimit(Restaurant restaurant) {
        // No limit in unlimited mode
    }

    @Override
    public void incrementChatUsage(Restaurant restaurant) {
        // No usage tracking in unlimited mode
    }

    @Override
    public boolean isUnlimited() {
        return true;
    }
}