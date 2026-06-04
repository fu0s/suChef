package com.example.SuChefService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "subscription_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SubscriptionPlan {
    @Id
    private String id;
    private String name;
    private BigDecimal price;
    private Long maxDocumentsSizeMb;
    private Integer maxChatsPerMonth;
    private Integer maxAccountsPerRestaurant;
    private Integer maxNotificationsPerMonth;
}
