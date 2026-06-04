package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private BigDecimal profitMargin;
    private Long documentCount;
    
    // New Metrics
    private BigDecimal bestOrder;
    private String highestNetMonth;
    private String stockWasteImprovement;
    private BigDecimal costOptimization;
    private BigDecimal wasteReduction;
    private String qualityTracking;
    
    // Graph Data represented as Key-Value Pairs or Lists
    private List<Map<String, Object>> revenueBreakdown;
    private List<Map<String, Object>> costAnalysis;
    private List<Map<String, Object>> profitMetrics;
    private List<Map<String, Object>> performanceTracking;
    private List<Map<String, Object>> topDishAnalytics;
    
    private BigDecimal marginOptimization;
}
