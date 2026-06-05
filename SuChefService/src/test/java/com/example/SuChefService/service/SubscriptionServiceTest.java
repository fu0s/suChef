package com.example.SuChefService.service;

import com.example.SuChefService.dto.RestaurantSubscriptionResponse;
import com.example.SuChefService.dto.SubscriptionPlanResponse;
import com.example.SuChefService.dto.SubscriptionUsageResponse;
import com.example.SuChefService.entity.*;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.exception.SubscriptionLimitExceededException;
import com.example.SuChefService.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionPlanRepository planRepository;

    @Mock
    private RestaurantSubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionUsageRepository usageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User currentUser;
    private Restaurant currentRestaurant;

    @BeforeEach
    void setUp() {
        currentRestaurant = Restaurant.builder()
                .id("rest-123")
                .name("Test Restaurant")
                .build();

        currentUser = User.builder()
                .id("user-123")
                .name("John Doe")
                .email("test@example.com")
                .restaurant(currentRestaurant)
                .build();

        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn("test@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkChatLimit_whenAllowed_shouldNotThrowException() {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id("free-plan-id")
                .name("Free")
                .maxChatsPerMonth(100)
                .build();
        RestaurantSubscription sub = RestaurantSubscription.builder()
                .restaurant(currentRestaurant)
                .plan(plan)
                .build();
        when(subscriptionRepository.findByRestaurant(currentRestaurant)).thenReturn(Optional.of(sub));

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .chatsCount(10)
                .monthYear(java.time.YearMonth.now().toString())
                .build();
        when(usageRepository.findByRestaurantAndMonthYear(eq(currentRestaurant), any())).thenReturn(Optional.of(usage));

        assertDoesNotThrow(() -> subscriptionService.checkChatLimit(currentRestaurant));
    }

    @Test
    void checkChatLimit_whenNotAllowed_shouldThrowSubscriptionLimitExceededException() {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id("free-plan-id")
                .name("Free")
                .maxChatsPerMonth(100)
                .build();
        RestaurantSubscription sub = RestaurantSubscription.builder()
                .restaurant(currentRestaurant)
                .plan(plan)
                .build();
        when(subscriptionRepository.findByRestaurant(currentRestaurant)).thenReturn(Optional.of(sub));

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .chatsCount(100)
                .monthYear(java.time.YearMonth.now().toString())
                .build();
        when(usageRepository.findByRestaurantAndMonthYear(eq(currentRestaurant), any())).thenReturn(Optional.of(usage));

        assertThrows(SubscriptionLimitExceededException.class,
                () -> subscriptionService.checkChatLimit(currentRestaurant));
    }

    @Test
    void checkChatLimit_whenNoSubscription_shouldNotThrow() {
        when(subscriptionRepository.findByRestaurant(currentRestaurant)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> subscriptionService.checkChatLimit(currentRestaurant));
    }

    @Test
    void checkDocumentLimit_whenAllowed_shouldNotThrowException() {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id("free-plan-id")
                .name("Free")
                .maxDocumentsSizeMb(50L)
                .build();
        RestaurantSubscription sub = RestaurantSubscription.builder()
                .restaurant(currentRestaurant)
                .plan(plan)
                .build();
        when(subscriptionRepository.findByRestaurant(currentRestaurant)).thenReturn(Optional.of(sub));

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .currentDocumentsSizeBytes(5L * 1024 * 1024)
                .monthYear(java.time.YearMonth.now().toString())
                .build();
        when(usageRepository.findByRestaurantAndMonthYear(eq(currentRestaurant), any())).thenReturn(Optional.of(usage));

        // sizeBytes = 1 MB, total = 6 MB < 50 MB limit
        assertDoesNotThrow(() -> subscriptionService.checkDocumentLimit(currentRestaurant, 1024 * 1024));
    }

    @Test
    void checkDocumentLimit_whenExceeded_shouldThrowSubscriptionLimitExceededException() {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id("free-plan-id")
                .name("Free")
                .maxDocumentsSizeMb(50L)
                .build();
        RestaurantSubscription sub = RestaurantSubscription.builder()
                .restaurant(currentRestaurant)
                .plan(plan)
                .build();
        when(subscriptionRepository.findByRestaurant(currentRestaurant)).thenReturn(Optional.of(sub));

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .currentDocumentsSizeBytes(49L * 1024 * 1024)
                .monthYear(java.time.YearMonth.now().toString())
                .build();
        when(usageRepository.findByRestaurantAndMonthYear(eq(currentRestaurant), any())).thenReturn(Optional.of(usage));

        // Requesting 2 MB which exceeds 50 MB limit (49MB + 2MB = 51MB > 50MB)
        assertThrows(SubscriptionLimitExceededException.class,
                () -> subscriptionService.checkDocumentLimit(currentRestaurant, 2 * 1024 * 1024));
    }

    @Test
    void checkDocumentLimit_whenNoSubscription_shouldNotThrow() {
        when(subscriptionRepository.findByRestaurant(currentRestaurant)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> subscriptionService.checkDocumentLimit(currentRestaurant, 1024 * 1024));
    }

    @Test
    void getCurrentUsage_success_shouldReturnResponse() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));

        SubscriptionUsage usage = SubscriptionUsage.builder()
                .id("usage-1")
                .monthYear(java.time.YearMonth.now().toString())
                .currentDocumentsSizeBytes(10L * 1024 * 1024)
                .chatsCount(5)
                .notificationsCount(15)
                .build();
        when(usageRepository.findByRestaurantAndMonthYear(eq(currentRestaurant), any())).thenReturn(Optional.of(usage));

        SubscriptionUsageResponse usageResponse = subscriptionService.getCurrentUsage();

        assertNotNull(usageResponse);
        assertEquals(5, usageResponse.getChatsCount());
        assertEquals(10 * 1024 * 1024L, usageResponse.getCurrentDocumentsSizeBytes());
        assertEquals(15, usageResponse.getNotificationsCount());
    }

    @Test
    void getCurrentUsage_noUsage_shouldReturnZeroDefaults() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));
        when(usageRepository.findByRestaurantAndMonthYear(eq(currentRestaurant), any())).thenReturn(Optional.empty());

        SubscriptionUsageResponse usageResponse = subscriptionService.getCurrentUsage();

        assertNotNull(usageResponse);
        assertEquals(0, usageResponse.getChatsCount());
        assertEquals(0L, usageResponse.getCurrentDocumentsSizeBytes());
        assertEquals(0, usageResponse.getNotificationsCount());
    }

    @Test
    void getCurrentSubscription_success_shouldReturnResponse() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id("plan-1")
                .name("Premium Plan")
                .price(BigDecimal.TEN)
                .maxDocumentsSizeMb(500L)
                .maxChatsPerMonth(1000)
                .maxAccountsPerRestaurant(10)
                .build();
        RestaurantSubscription sub = RestaurantSubscription.builder()
                .id("sub-1")
                .restaurant(currentRestaurant)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .build();
        when(subscriptionRepository.findByRestaurant(currentRestaurant)).thenReturn(Optional.of(sub));

        RestaurantSubscriptionResponse subscriptionResponse = subscriptionService.getCurrentSubscription();

        assertNotNull(subscriptionResponse);
        assertEquals("rest-123", subscriptionResponse.getRestaurantId());
        assertEquals("Premium Plan", subscriptionResponse.getPlan().getName());
        assertEquals(BigDecimal.TEN, subscriptionResponse.getPlan().getPrice());
        assertEquals(500, subscriptionResponse.getPlan().getMaxDocumentsSizeMb());
        assertEquals(1000, subscriptionResponse.getPlan().getMaxChatsPerMonth());
        assertEquals(10, subscriptionResponse.getPlan().getMaxAccountsPerRestaurant());
    }

    @Test
    void getAllPlans_success_shouldReturnMappedList() {
        SubscriptionPlan plan1 = SubscriptionPlan.builder()
                .id("plan-1").name("Free").price(BigDecimal.ZERO)
                .maxDocumentsSizeMb(50L).maxChatsPerMonth(100).maxAccountsPerRestaurant(3).maxNotificationsPerMonth(50)
                .build();
        SubscriptionPlan plan2 = SubscriptionPlan.builder()
                .id("plan-2").name("Pro").price(BigDecimal.TEN)
                .maxDocumentsSizeMb(500L).maxChatsPerMonth(1000).maxAccountsPerRestaurant(10).maxNotificationsPerMonth(500)
                .build();
        when(planRepository.findAll()).thenReturn(Arrays.asList(plan1, plan2));

        List<SubscriptionPlanResponse> plans = subscriptionService.getAllPlans();

        assertNotNull(plans);
        assertEquals(2, plans.size());
        assertEquals("Free", plans.get(0).getName());
        assertEquals("Pro", plans.get(1).getName());
    }
}
