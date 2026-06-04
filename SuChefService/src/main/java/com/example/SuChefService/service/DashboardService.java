package com.example.SuChefService.service;

import com.example.SuChefService.dto.DashboardMetricsResponse;
import com.example.SuChefService.entity.*;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    // For a real production app we'd use custom JPQL grouping queries for performance,
    // but Stream aggregation is fine for the MVP dashboard.

    public DashboardMetricsResponse getMetricsForRestaurant(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (currentUser == null || currentUser.getRestaurant() == null) {
            throw new ResourceNotFoundException("User or restaurant not found");
        }
        Restaurant restaurant = currentUser.getRestaurant();
        List<Order> orders = orderRepository.findByRestaurant(restaurant);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal bestOrder = BigDecimal.ZERO;
        
        Map<String, BigDecimal> revenueByMonth = new HashMap<>();
        Map<String, Long> dishCount = new HashMap<>();
        
        for (Order order : orders) {
            BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(amount);
            
            if (amount.compareTo(bestOrder) > 0) {
                bestOrder = amount;
            }
            
            if (order.getOrderDate() != null) {
                String monthKey = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                revenueByMonth.put(monthKey, revenueByMonth.getOrDefault(monthKey, BigDecimal.ZERO).add(amount));
            }
            
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getMenuItem() != null) {
                        String dishName = item.getMenuItem().getName();
                        dishCount.put(dishName, dishCount.getOrDefault(dishName, 0L) + (item.getQuantity() != null ? item.getQuantity() : 1));
                    }
                }
            }
        }
        
        long totalOrders = orders.size();
        BigDecimal avgOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;

        // Find Highest Net Month
        String highestNetMonth = "N/A";
        BigDecimal maxMonthRev = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : revenueByMonth.entrySet()) {
            if (entry.getValue().compareTo(maxMonthRev) > 0) {
                maxMonthRev = entry.getValue();
                highestNetMonth = entry.getKey();
            }
        }

        // Calculate mocked but realistic companion metrics based on the real revenue
        BigDecimal totalExpenses = totalRevenue.multiply(BigDecimal.valueOf(0.65)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal profit = totalRevenue.subtract(totalExpenses);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 
                ? profit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Build Graph Data
        List<Map<String, Object>> topDishesList = dishCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", e.getKey());
                    map.put("value", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> perfTracking = revenueByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("month", e.getKey());
                    map.put("revenue", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        // Dummy data for complex charts to show the UI
        List<Map<String, Object>> revenueBreakdown = List.of(
            Map.of("category", "Dine-in", "amount", totalRevenue.multiply(BigDecimal.valueOf(0.7))),
            Map.of("category", "Takeout", "amount", totalRevenue.multiply(BigDecimal.valueOf(0.2))),
            Map.of("category", "Delivery", "amount", totalRevenue.multiply(BigDecimal.valueOf(0.1)))
        );
        
        List<Map<String, Object>> costAnalysis = List.of(
            Map.of("category", "Ingredients", "amount", totalExpenses.multiply(BigDecimal.valueOf(0.4))),
            Map.of("category", "Labor", "amount", totalExpenses.multiply(BigDecimal.valueOf(0.3))),
            Map.of("category", "Rent & Utils", "amount", totalExpenses.multiply(BigDecimal.valueOf(0.3)))
        );

        long docCount = documentRepository.count();

        return DashboardMetricsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .profitMargin(profitMargin)
                .documentCount(docCount)
                .bestOrder(bestOrder)
                .highestNetMonth(highestNetMonth)
                .stockWasteImprovement("15% reduction")
                .costOptimization(BigDecimal.valueOf(1250.00))
                .wasteReduction(BigDecimal.valueOf(45.5))
                .qualityTracking("98% Positive")
                .marginOptimization(BigDecimal.valueOf(2.5))
                .revenueBreakdown(revenueBreakdown)
                .costAnalysis(costAnalysis)
                .profitMetrics(List.of(Map.of("Profit", profit)))
                .performanceTracking(perfTracking)
                .topDishAnalytics(topDishesList)
                .build();
    }
}
