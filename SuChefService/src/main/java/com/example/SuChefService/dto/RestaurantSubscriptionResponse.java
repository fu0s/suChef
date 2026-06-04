package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSubscriptionResponse {
    private String id;
    private String restaurantId;
    private SubscriptionPlanResponse plan;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
