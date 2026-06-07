package com.example.SuChefService.chat;

import com.example.SuChefService.entity.Restaurant;

public interface ChatUsagePolicy {
    
    Restaurant getCurrentRestaurant();
    
    void checkChatLimit(Restaurant restaurant);
    
    void incrementChatUsage(Restaurant restaurant);
    
    boolean isUnlimited();
}