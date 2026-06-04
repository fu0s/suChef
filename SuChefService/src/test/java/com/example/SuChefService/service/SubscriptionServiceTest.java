package com.example.SuChefService.service;

import com.example.SuChefService.dto.RestaurantSubscriptionResponse;
import com.example.SuChefService.dto.SubscriptionPlanResponse;
import com.example.SuChefService.dto.SubscriptionUsageResponse;
import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.User;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.exception.SubscriptionLimitExceededException;
import com.example.SuChefService.mcp.McpToolProvider;
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

    @Mock
    private McpToolProvider mcpToolProvider;

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
        McpToolProvider.SubscriptionLimitCheckResponse allowedResponse =
                new McpToolProvider.SubscriptionLimitCheckResponse(true, "CHAT", 10, 100, 90);
        when(mcpToolProvider.checkSubscriptionLimit("CHAT")).thenReturn(allowedResponse);

        assertDoesNotThrow(() -> subscriptionService.checkChatLimit(currentRestaurant));

        verify(mcpToolProvider).checkSubscriptionLimit("CHAT");
    }

    @Test
    void checkChatLimit_whenNotAllowed_shouldThrowSubscriptionLimitExceededException() {
        McpToolProvider.SubscriptionLimitCheckResponse blockedResponse =
                new McpToolProvider.SubscriptionLimitCheckResponse(false, "CHAT", 100, 100, 0);
        when(mcpToolProvider.checkSubscriptionLimit("CHAT")).thenReturn(blockedResponse);

        assertThrows(SubscriptionLimitExceededException.class,
                () -> subscriptionService.checkChatLimit(currentRestaurant));

        verify(mcpToolProvider).checkSubscriptionLimit("CHAT");
    }

    @Test
    void checkChatLimit_onMcpError_shouldLogAndNotThrow() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", "VALIDATION_ERROR");
        errorMap.put("message", "Something went wrong");
        when(mcpToolProvider.checkSubscriptionLimit("CHAT")).thenReturn(errorMap);

        assertDoesNotThrow(() -> subscriptionService.checkChatLimit(currentRestaurant));

        verify(mcpToolProvider).checkSubscriptionLimit("CHAT");
    }

    @Test
    void checkDocumentLimit_whenAllowed_shouldNotThrowException() {
        McpToolProvider.SubscriptionLimitCheckResponse allowedResponse =
                new McpToolProvider.SubscriptionLimitCheckResponse(true, "DOCUMENT_UPLOAD", 5, 50, 45); // Usage in MB
        when(mcpToolProvider.checkSubscriptionLimit("DOCUMENT_UPLOAD")).thenReturn(allowedResponse);

        // sizeBytes = 1 MB (1024 * 1024 bytes)
        assertDoesNotThrow(() -> subscriptionService.checkDocumentLimit(currentRestaurant, 1024 * 1024));

        verify(mcpToolProvider).checkSubscriptionLimit("DOCUMENT_UPLOAD");
    }

    @Test
    void checkDocumentLimit_whenExceeded_shouldThrowSubscriptionLimitExceededException() {
        McpToolProvider.SubscriptionLimitCheckResponse limitResponse =
                new McpToolProvider.SubscriptionLimitCheckResponse(true, "DOCUMENT_UPLOAD", 49, 50, 1); // Usage in MB
        when(mcpToolProvider.checkSubscriptionLimit("DOCUMENT_UPLOAD")).thenReturn(limitResponse);

        // Requesting 2 MB which exceeds 50 MB limit (49MB + 2MB = 51MB > 50MB)
        assertThrows(SubscriptionLimitExceededException.class,
                () -> subscriptionService.checkDocumentLimit(currentRestaurant, 2 * 1024 * 1024));

        verify(mcpToolProvider).checkSubscriptionLimit("DOCUMENT_UPLOAD");
    }

    @Test
    void checkDocumentLimit_onMcpError_shouldLogAndNotThrow() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", "VALIDATION_ERROR");
        errorMap.put("message", "Something went wrong");
        when(mcpToolProvider.checkSubscriptionLimit("DOCUMENT_UPLOAD")).thenReturn(errorMap);

        assertDoesNotThrow(() -> subscriptionService.checkDocumentLimit(currentRestaurant, 1024 * 1024));

        verify(mcpToolProvider).checkSubscriptionLimit("DOCUMENT_UPLOAD");
    }

    @Test
    void getCurrentUsage_success_shouldReturnResponse() {
        McpToolProvider.SubscriptionInfo info = new McpToolProvider.SubscriptionInfo(
                "Free Plan", BigDecimal.ZERO, 50L, 100, 3, 10.0, 5, 15, LocalDateTime.now(), null
        );
        when(mcpToolProvider.getSubscriptionInfo()).thenReturn(info);

        SubscriptionUsageResponse usageResponse = subscriptionService.getCurrentUsage();

        assertNotNull(usageResponse);
        assertEquals(5, usageResponse.getChatsCount());
        assertEquals(10 * 1024 * 1024L, usageResponse.getCurrentDocumentsSizeBytes());
        assertEquals(15, usageResponse.getNotificationsCount());
    }

    @Test
    void getCurrentUsage_onMcpError_shouldThrowResourceNotFoundException() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", "VALIDATION_ERROR");
        errorMap.put("message", "Invalid token");
        when(mcpToolProvider.getSubscriptionInfo()).thenReturn(errorMap);

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.getCurrentUsage());
    }

    @Test
    void getCurrentSubscription_success_shouldReturnResponse() {
        McpToolProvider.SubscriptionInfo info = new McpToolProvider.SubscriptionInfo(
                "Premium Plan", BigDecimal.TEN, 500L, 1000, 10, 50.0, 25, 100, LocalDateTime.now(), null
        );
        when(mcpToolProvider.getSubscriptionInfo()).thenReturn(info);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));

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
        McpToolProvider.SubscriptionPlanInfo plan1 = new McpToolProvider.SubscriptionPlanInfo(
                "plan-1", "Free", BigDecimal.ZERO, 50L, 100, 3, 50
        );
        McpToolProvider.SubscriptionPlanInfo plan2 = new McpToolProvider.SubscriptionPlanInfo(
                "plan-2", "Pro", BigDecimal.TEN, 500L, 1000, 10, 500
        );
        when(mcpToolProvider.getAllPlans()).thenReturn(Arrays.asList(plan1, plan2));

        List<SubscriptionPlanResponse> plans = subscriptionService.getAllPlans();

        assertNotNull(plans);
        assertEquals(2, plans.size());
        assertEquals("Free", plans.get(0).getName());
        assertEquals("Pro", plans.get(1).getName());
    }

    @Test
    void getAllPlans_onMcpError_shouldThrowResourceNotFoundException() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", "NOT_FOUND");
        errorMap.put("message", "Plans not found");
        when(mcpToolProvider.getAllPlans()).thenReturn(errorMap);

        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.getAllPlans());
    }
}
