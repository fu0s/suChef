package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private Long maxDocumentsSizeMb;
    private Integer maxChatsPerMonth;
    private Integer maxAccountsPerRestaurant;
    private Integer maxNotificationsPerMonth;
}
