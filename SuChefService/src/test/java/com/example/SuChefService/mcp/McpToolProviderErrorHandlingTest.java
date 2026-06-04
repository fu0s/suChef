package com.example.SuChefService.mcp;

import com.example.SuChefService.entity.*;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class McpToolProviderErrorHandlingTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private VendorRepository vendorRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantMetricRepository restaurantMetricRepository;
    @Mock private StockTransactionRepository stockTransactionRepository;
    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private RestaurantSubscriptionRepository restaurantSubscriptionRepository;
    @Mock private SubscriptionUsageRepository subscriptionUsageRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private McpToolProvider mcpToolProvider;

    private User testUser;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = Restaurant.builder()
                .id("rest-1").name("Test Restaurant").location("123 Main St").build();
        testUser = User.builder()
                .id("user-1").email("test@example.com").name("Test User").restaurant(testRestaurant).build();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);
        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    // ==================== Structured Error Responses — Not Found ====================

    @Test
    @SuppressWarnings("unchecked")
    void getDocumentById_returnsStructuredNotFound() {
        when(documentRepository.findById("bad-id")).thenReturn(Optional.empty());
        Object result = mcpToolProvider.getDocumentById("bad-id");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("NOT_FOUND", error.get("error"));
        assertEquals("Document", error.get("resource"));
        assertEquals("bad-id", error.get("id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getVendorById_returnsStructuredNotFound() {
        when(vendorRepository.findById("bad-id")).thenReturn(Optional.empty());
        Object result = mcpToolProvider.getVendorById("bad-id");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("NOT_FOUND", error.get("error"));
        assertEquals("Vendor", error.get("resource"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getInventoryItemById_returnsStructuredNotFound() {
        when(inventoryItemRepository.findById("bad-id")).thenReturn(Optional.empty());
        Object result = mcpToolProvider.getInventoryItemById("bad-id");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("NOT_FOUND", error.get("error"));
        assertEquals("Inventory item", error.get("resource"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrderById_returnsStructuredNotFound() {
        when(orderRepository.findById("bad-id")).thenReturn(Optional.empty());
        Object result = mcpToolProvider.getOrderById("bad-id");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("NOT_FOUND", error.get("error"));
        assertEquals("Order", error.get("resource"));
    }

    // ==================== Structured Error Responses — Access Denied ====================

    @Test
    @SuppressWarnings("unchecked")
    void getDocumentById_returnsAccessDeniedForWrongOwner() {
        User other = User.builder().id("user-2").build();
        Document doc = Document.builder().id("doc-1").user(other).build();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        Object result = mcpToolProvider.getDocumentById("doc-1");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("ACCESS_DENIED", error.get("error"));
        assertEquals("Document", error.get("resource"));
        assertEquals("doc-1", error.get("id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getVendorById_returnsAccessDeniedForWrongOwner() {
        User other = User.builder().id("user-2").build();
        Vendor vendor = Vendor.builder().id("v-1").user(other).build();
        when(vendorRepository.findById("v-1")).thenReturn(Optional.of(vendor));
        Object result = mcpToolProvider.getVendorById("v-1");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("ACCESS_DENIED", error.get("error"));
        assertEquals("Vendor", error.get("resource"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrderById_returnsAccessDeniedForWrongRestaurant() {
        Restaurant other = Restaurant.builder().id("rest-2").build();
        Order order = Order.builder().id("o-1").restaurant(other).build();
        when(orderRepository.findByIdWithItems("o-1")).thenReturn(Optional.of(order));
        Object result = mcpToolProvider.getOrderById("o-1");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("ACCESS_DENIED", error.get("error"));
        assertEquals("Order", error.get("resource"));
    }

    // ==================== Structured Error Responses — Validation ====================

    @Test
    @SuppressWarnings("unchecked")
    void getDocumentsByStatus_returnsValidationErrorForInvalidStatus() {
        Object result = mcpToolProvider.getDocumentsByStatus("INVALID_STATUS");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("VALIDATION_ERROR", error.get("error"));
        assertEquals("status", error.get("field"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void checkSubscriptionLimit_returnsValidationErrorForInvalidActionType() {
        Restaurant restaurant = getCurrentRestaurant();
        SubscriptionPlan plan = SubscriptionPlan.builder().id("p-1").maxChatsPerMonth(100).build();
        RestaurantSubscription sub = RestaurantSubscription.builder().restaurant(testRestaurant).plan(plan).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.empty());

        Object result = mcpToolProvider.checkSubscriptionLimit("INVALID_ACTION");
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("VALIDATION_ERROR", error.get("error"));
        assertEquals("actionType", error.get("field"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void incrementUsage_returnsValidationErrorForInvalidActionType() {
        Restaurant restaurant = getCurrentRestaurant();
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.empty());
        when(subscriptionUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Object result = mcpToolProvider.incrementUsage("UNKNOWN_TYPE", null);
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("VALIDATION_ERROR", error.get("error"));
        assertEquals("actionType", error.get("field"));
    }

    // ==================== Successful Tools Return Proper Types ====================

    @Test
    void getDocumentCount_returnsProperType() {
        when(documentRepository.countByUser(testUser)).thenReturn(5L);
        Object result = mcpToolProvider.getDocumentCount();
        assertInstanceOf(McpToolProvider.DocumentCountResponse.class, result);
        assertEquals(5L, ((McpToolProvider.DocumentCountResponse) result).count());
    }

    @Test
    void getVendors_returnsProperType() {
        Vendor v = Vendor.builder().id("v-1").name("Fresh Farms").category("produce").user(testUser).build();
        when(vendorRepository.findByUser(testUser)).thenReturn(List.of(v));
        Object result = mcpToolProvider.getVendors();
        assertInstanceOf(List.class, result);
        @SuppressWarnings("unchecked")
        List<McpToolProvider.VendorInfo> list = (List<McpToolProvider.VendorInfo>) result;
        assertEquals(1, list.size());
        assertEquals("Fresh Farms", list.get(0).name());
    }

    @Test
    void getInventoryItems_returnsProperType() {
        InventoryItem item = InventoryItem.builder().id("i-1").name("Tomatoes").currentStock(BigDecimal.TEN).unit("kg").user(testUser).build();
        when(inventoryItemRepository.findByUser(testUser)).thenReturn(List.of(item));
        Object result = mcpToolProvider.getInventoryItems();
        assertInstanceOf(List.class, result);
    }

    @Test
    void createVendor_returnsProperType() {
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(inv -> inv.getArgument(0));
        Object result = mcpToolProvider.createVendor("Fresh Farms", "produce", "555-0100");
        assertInstanceOf(McpToolProvider.VendorCreateResponse.class, result);
    }

    // ==================== Restaurant Not Found ====================

    @Test
    @SuppressWarnings("unchecked")
    void getRecentOrders_returnsNotFoundWhenNoRestaurant() {
        User userNoRestaurant = User.builder().id("user-2").email("no-restaurant@example.com").restaurant(null).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userNoRestaurant));
        Object result = mcpToolProvider.getRecentOrders();
        assertInstanceOf(Map.class, result);
        Map<String, Object> error = (Map<String, Object>) result;
        assertEquals("NOT_FOUND", error.get("error"));
        assertEquals("Restaurant", error.get("resource"));
    }

    private Restaurant getCurrentRestaurant() {
        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        return testRestaurant;
    }
}
