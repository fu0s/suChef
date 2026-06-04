package com.example.SuChefService.service;

import com.example.SuChefService.dto.RestaurantSubscriptionResponse;
import com.example.SuChefService.dto.SubscriptionPlanResponse;
import com.example.SuChefService.dto.SubscriptionUsageResponse;
import com.example.SuChefService.entity.*;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.exception.SubscriptionLimitExceededException;
import com.example.SuChefService.mcp.McpToolProvider;
import com.example.SuChefService.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final RestaurantSubscriptionRepository subscriptionRepository;
    private final SubscriptionUsageRepository usageRepository;
    private final UserRepository userRepository;
    private final McpToolProvider mcpToolProvider;

    private static final String DEFAULT_PLAN_ID = "free-plan-id";
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public Restaurant getCurrentRestaurant() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRestaurant() == null) {
            throw new com.example.SuChefService.exception.NoRestaurantException("User has no restaurant, please set up a restaurant first.");
        }
        return user.getRestaurant();
    }

    public RestaurantSubscription getSubscription(Restaurant restaurant) {
        return subscriptionRepository.findByRestaurant(restaurant)
                .orElseGet(() -> {
                    SubscriptionPlan freePlan = planRepository.findById(DEFAULT_PLAN_ID)
                            .orElseThrow(() -> new ResourceNotFoundException("Default plan not found"));
                    RestaurantSubscription sub = RestaurantSubscription.builder()
                            .id(UUID.randomUUID().toString())
                            .restaurant(restaurant)
                            .plan(freePlan)
                            .startDate(LocalDateTime.now())
                            .build();
                    return subscriptionRepository.save(sub);
                });
    }

    public SubscriptionUsage getOrCreateUsage(Restaurant restaurant) {
        String monthYear = LocalDateTime.now().format(MONTH_YEAR_FORMATTER);
        return usageRepository.findByRestaurantAndMonthYear(restaurant, monthYear)
                .orElseGet(() -> {
                    SubscriptionUsage usage = SubscriptionUsage.builder()
                            .id(UUID.randomUUID().toString())
                            .restaurant(restaurant)
                            .monthYear(monthYear)
                            .currentDocumentsSizeBytes(0L)
                            .chatsCount(0)
                            .notificationsCount(0)
                            .build();
                    return usageRepository.save(usage);
                });
    }

    @Transactional
    public void incrementChatUsage(Restaurant restaurant) {
        checkChatLimit(restaurant);
        mcpToolProvider.incrementUsage("CHAT", 1);
    }

    @Transactional
    public void incrementDocumentUsage(Restaurant restaurant, long sizeBytes) {
        checkDocumentLimit(restaurant, sizeBytes);
        mcpToolProvider.incrementUsage("DOCUMENT_UPLOAD", (int) sizeBytes);
    }

    public void checkChatLimit(Restaurant restaurant) {
        Object result = mcpToolProvider.checkSubscriptionLimit("CHAT");
        if (result instanceof Map<?, ?> map && map.containsKey("error")) {
            log.warn("MCP checkSubscriptionLimit returned error: {}", map.get("message"));
            return;
        }
        McpToolProvider.SubscriptionLimitCheckResponse response =
                (McpToolProvider.SubscriptionLimitCheckResponse) result;
        if (!response.allowed()) {
            throw new SubscriptionLimitExceededException(
                    "Monthly chat limit reached. Usage: " + response.currentUsage() + "/" + response.limit());
        }
    }

    public void checkDocumentLimit(Restaurant restaurant, long sizeBytes) {
        Object result = mcpToolProvider.checkSubscriptionLimit("DOCUMENT_UPLOAD");
        if (result instanceof Map<?, ?> map && map.containsKey("error")) {
            log.warn("MCP checkSubscriptionLimit returned error: {}", map.get("message"));
            return;
        }
        McpToolProvider.SubscriptionLimitCheckResponse response =
                (McpToolProvider.SubscriptionLimitCheckResponse) result;
        long currentBytes = (long) response.currentUsage() * 1024 * 1024;
        long maxBytes = (long) response.limit() * 1024 * 1024;
        if (currentBytes + sizeBytes > maxBytes) {
            throw new SubscriptionLimitExceededException(
                    "Total document size limit reached. Usage: " + response.currentUsage() + "MB/" + response.limit() + "MB");
        }
    }

    public void checkAccountLimit(Restaurant restaurant) {
        long count = userRepository.countByRestaurant(restaurant);
        SubscriptionPlan plan = getSubscription(restaurant).getPlan();
        int limit = plan.getMaxAccountsPerRestaurant() != null ? plan.getMaxAccountsPerRestaurant() : 0;
        if (count >= limit) {
            throw new SubscriptionLimitExceededException(
                    "Account limit reached. Usage: " + count + "/" + limit);
        }
    }

    public SubscriptionUsageResponse getCurrentUsage() {
        Object result = mcpToolProvider.getSubscriptionInfo();
        if (result instanceof Map<?, ?> map && map.containsKey("error")) {
            throw new ResourceNotFoundException("Subscription info not available: " + map.get("message"));
        }
        McpToolProvider.SubscriptionInfo info = (McpToolProvider.SubscriptionInfo) result;
        if (info == null) {
            throw new ResourceNotFoundException("Subscription info not available");
        }
        return SubscriptionUsageResponse.builder()
                .id("mcp-delegated")
                .monthYear(java.time.YearMonth.now().toString())
                .currentDocumentsSizeBytes(info.currentDocumentsSizeMb() != null
                        ? (long) (info.currentDocumentsSizeMb() * 1024 * 1024) : 0L)
                .chatsCount(info.currentChatsCount() != null ? info.currentChatsCount() : 0)
                .notificationsCount(info.currentNotificationsCount() != null ? info.currentNotificationsCount() : 0)
                .build();
    }

    public RestaurantSubscriptionResponse getCurrentSubscription() {
        Object result = mcpToolProvider.getSubscriptionInfo();
        if (result instanceof Map<?, ?> map && map.containsKey("error")) {
            throw new ResourceNotFoundException("Subscription info not available: " + map.get("message"));
        }
        McpToolProvider.SubscriptionInfo info = (McpToolProvider.SubscriptionInfo) result;
        if (info == null) {
            throw new ResourceNotFoundException("Subscription info not available");
        }
        Restaurant restaurant = getCurrentRestaurant();
        return RestaurantSubscriptionResponse.builder()
                .id("mcp-delegated")
                .restaurantId(restaurant.getId())
                .plan(SubscriptionPlanResponse.builder()
                        .id("mcp-plan")
                        .name(info.planName())
                        .price(info.planPrice())
                        .maxDocumentsSizeMb(info.maxDocumentsSizeMb())
                        .maxChatsPerMonth(info.maxChatsPerMonth())
                        .maxAccountsPerRestaurant(info.maxAccountsPerRestaurant())
                        .build())
                .startDate(info.startDate())
                .endDate(info.endDate())
                .build();
    }

    public List<SubscriptionPlanResponse> getAllPlans() {
        Object result = mcpToolProvider.getAllPlans();
        if (result instanceof Map<?, ?> map && map.containsKey("error")) {
            throw new ResourceNotFoundException("Plans not available: " + map.get("message"));
        }
        @SuppressWarnings("unchecked")
        List<McpToolProvider.SubscriptionPlanInfo> plans =
                (List<McpToolProvider.SubscriptionPlanInfo>) (List<?>) result;
        return plans.stream()
                .map(p -> SubscriptionPlanResponse.builder()
                        .id(p.id())
                        .name(p.name())
                        .price(p.price())
                        .maxDocumentsSizeMb(p.maxDocumentsSizeMb())
                        .maxChatsPerMonth(p.maxChatsPerMonth())
                        .maxAccountsPerRestaurant(p.maxAccountsPerRestaurant())
                        .maxNotificationsPerMonth(p.maxNotificationsPerMonth())
                        .build())
                .collect(Collectors.toList());
    }

    private SubscriptionPlanResponse toPlanResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .maxDocumentsSizeMb(plan.getMaxDocumentsSizeMb())
                .maxChatsPerMonth(plan.getMaxChatsPerMonth())
                .maxAccountsPerRestaurant(plan.getMaxAccountsPerRestaurant())
                .maxNotificationsPerMonth(plan.getMaxNotificationsPerMonth())
                .build();
    }

    private SubscriptionUsageResponse toUsageResponse(SubscriptionUsage usage) {
        return SubscriptionUsageResponse.builder()
                .id(usage.getId())
                .monthYear(usage.getMonthYear())
                .currentDocumentsSizeBytes(usage.getCurrentDocumentsSizeBytes())
                .chatsCount(usage.getChatsCount())
                .notificationsCount(usage.getNotificationsCount())
                .build();
    }

    private RestaurantSubscriptionResponse toSubscriptionResponse(RestaurantSubscription sub) {
        return RestaurantSubscriptionResponse.builder()
                .id(sub.getId())
                .restaurantId(sub.getRestaurant().getId())
                .plan(toPlanResponse(sub.getPlan()))
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .build();
    }
}
