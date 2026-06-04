package com.example.SuChefService.controller;

import com.example.SuChefService.dto.DashboardMetricsResponse;
import com.example.SuChefService.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsResponse> getMetrics(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest().build();
        }
        DashboardMetricsResponse metrics = dashboardService.getMetricsForRestaurant(authentication);
        return ResponseEntity.ok(metrics);
    }
}
