package com.example.SuChefService.service;

import com.example.SuChefService.dto.RestaurantSubscriptionResponse;
import com.example.SuChefService.dto.SubscriptionPlanResponse;
import com.example.SuChefService.dto.SubscriptionUsageResponse;
import com.example.SuChefService.entity.*;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.exception.SubscriptionLimitExceededException;
import com.example.SuChefService.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        String monthYear = YearMonth.now().toString();
        SubscriptionUsage usage = usageRepository.findByRestaurantAndMonthYear(restaurant, monthYear)
                .orElseGet(() -> {
                    SubscriptionUsage newUsage = SubscriptionUsage.builder()
                            .id(UUID.randomUUID().toString())
                            .restaurant(restaurant)
                            .monthYear(monthYear)
                            .currentDocumentsSizeBytes(0L)
                            .chatsCount(0)
                            .notificationsCount(0)
                            .build();
                    return usageRepository.save(newUsage);
                });
        usage.setChatsCount((usage.getChatsCount() != null ? usage.getChatsCount() : 0) + 1);
        usageRepository.save(usage);
    }

    @Transactional
    public void incrementDocumentUsage(Restaurant restaurant, long sizeBytes) {
        checkDocumentLimit(restaurant, sizeBytes);
        String monthYear = YearMonth.now().toString();
        SubscriptionUsage usage = usageRepository.findByRestaurantAndMonthYear(restaurant, monthYear)
                .orElseGet(() -> {
                    SubscriptionUsage newUsage = SubscriptionUsage.builder()
                            .id(UUID.randomUUID().toString())
                            .restaurant(restaurant)
                            .monthYear(monthYear)
                            .currentDocumentsSizeBytes(0L)
                            .chatsCount(0)
                            .notificationsCount(0)
                            .build();
                    return usageRepository.save(newUsage);
                });
        usage.setCurrentDocumentsSizeBytes(
                (usage.getCurrentDocumentsSizeBytes() != null ? usage.getCurrentDocumentsSizeBytes() : 0L) + sizeBytes);
        usageRepository.save(usage);
    }

    public void checkChatLimit(Restaurant restaurant) {
        RestaurantSubscription sub = subscriptionRepository.findByRestaurant(restaurant).orElse(null);
        if (sub == null || sub.getPlan() == null) {
            return;
        }
        String monthYear = YearMonth.now().toString();
        SubscriptionUsage usage = usageRepository.findByRestaurantAndMonthYear(restaurant, monthYear).orElse(null);
        int currentUsage = usage != null && usage.getChatsCount() != null ? usage.getChatsCount() : 0;
        int limit = sub.getPlan().getMaxChatsPerMonth() != null ? sub.getPlan().getMaxChatsPerMonth() : 0;
        if (limit > 0 && currentUsage >= limit) {
            throw new SubscriptionLimitExceededException(
                    "Monthly chat limit reached. Usage: " + currentUsage + "/" + limit);
        }
    }

    public void checkDocumentLimit(Restaurant restaurant, long sizeBytes) {
        RestaurantSubscription sub = subscriptionRepository.findByRestaurant(restaurant).orElse(null);
        if (sub == null || sub.getPlan() == null) {
            return;
        }
        String monthYear = YearMonth.now().toString();
        SubscriptionUsage usage = usageRepository.findByRestaurantAndMonthYear(restaurant, monthYear).orElse(null);
        long currentBytes = usage != null && usage.getCurrentDocumentsSizeBytes() != null ? usage.getCurrentDocumentsSizeBytes() : 0L;
        long maxBytes = sub.getPlan().getMaxDocumentsSizeMb() != null ? sub.getPlan().getMaxDocumentsSizeMb() * 1024 * 1024 : 0;
        if (maxBytes > 0 && currentBytes + sizeBytes > maxBytes) {
            long currentMb = currentBytes / (1024 * 1024);
            long maxMb = maxBytes / (1024 * 1024);
            throw new SubscriptionLimitExceededException(
                    "Total document size limit reached. Usage: " + currentMb + "MB/" + maxMb + "MB");
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
        Restaurant restaurant = getCurrentRestaurant();
        String monthYear = YearMonth.now().toString();
        SubscriptionUsage usage = usageRepository.findByRestaurantAndMonthYear(restaurant, monthYear).orElse(null);
        if (usage == null) {
            return SubscriptionUsageResponse.builder()
                    .id("none")
                    .monthYear(monthYear)
                    .currentDocumentsSizeBytes(0L)
                    .chatsCount(0)
                    .notificationsCount(0)
                    .build();
        }
        return toUsageResponse(usage);
    }

    public RestaurantSubscriptionResponse getCurrentSubscription() {
        Restaurant restaurant = getCurrentRestaurant();
        RestaurantSubscription sub = getSubscription(restaurant);
        return toSubscriptionResponse(sub);
    }

    public List<SubscriptionPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toPlanResponse)
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
