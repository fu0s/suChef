package com.example.SuChefService.chat;

import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBasedChatUsagePolicy implements ChatUsagePolicy {

    private final SubscriptionService subscriptionService;

    @Override
    public Restaurant getCurrentRestaurant() {
        return subscriptionService.getCurrentRestaurant();
    }

    @Override
    public void checkChatLimit(Restaurant restaurant) {
        subscriptionService.checkChatLimit(restaurant);
    }

    @Override
    public void incrementChatUsage(Restaurant restaurant) {
        subscriptionService.incrementChatUsage(restaurant);
    }

    @Override
    public boolean isUnlimited() {
        return false;
    }
}