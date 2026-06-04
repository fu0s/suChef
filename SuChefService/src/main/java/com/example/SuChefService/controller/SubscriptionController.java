package com.example.SuChefService.controller;

import com.example.SuChefService.dto.RestaurantSubscriptionResponse;
import com.example.SuChefService.dto.SubscriptionPlanResponse;
import com.example.SuChefService.dto.SubscriptionUsageResponse;
import com.example.SuChefService.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/usage")
    public SubscriptionUsageResponse getUsage() {
        return subscriptionService.getCurrentUsage();
    }

    @GetMapping("/current")
    public RestaurantSubscriptionResponse getCurrentSubscription() {
        return subscriptionService.getCurrentSubscription();
    }

    @GetMapping("/plans")
    public List<SubscriptionPlanResponse> getAllPlans() {
        return subscriptionService.getAllPlans();
    }
}
