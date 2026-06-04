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
class McpToolProviderTest {

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

    // ==================== Document Read Tools ====================

    @Test
    void getDocumentCount_returnsCount() {
        when(documentRepository.countByUser(testUser)).thenReturn(5L);
        var result = (McpToolProvider.DocumentCountResponse) mcpToolProvider.getDocumentCount();
        assertEquals(5L, result.count());
    }

    @Test
    void getDocumentById_returnsDocument() {
        Document doc = Document.builder().id("doc-1").name("receipt.pdf").status(DocumentStatus.COMPLETED).user(testUser).build();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        var result = (McpToolProvider.DocumentInfo) mcpToolProvider.getDocumentById("doc-1");
        assertEquals("doc-1", result.id());
        assertEquals("receipt.pdf", result.name());
    }

    @Test
    void getDocumentById_throwsWhenNotFound() {
        when(documentRepository.findById("bad-id")).thenReturn(Optional.empty());
        var result = mcpToolProvider.getDocumentById("bad-id");
        assertInstanceOf(Map.class, result);
        assertEquals("NOT_FOUND", ((Map<?, ?>) result).get("error"));
    }

    @Test
    void getDocumentById_throwsWhenWrongOwner() {
        User other = User.builder().id("user-2").build();
        Document doc = Document.builder().id("doc-1").user(other).build();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        var result = mcpToolProvider.getDocumentById("doc-1");
        assertInstanceOf(Map.class, result);
        assertEquals("ACCESS_DENIED", ((Map<?, ?>) result).get("error"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getDocumentsByStatus_returnsFiltered() {
        Document doc = Document.builder().id("doc-1").name("bill.pdf").status(DocumentStatus.COMPLETED).user(testUser).build();
        when(documentRepository.findByUserAndStatus(testUser, DocumentStatus.COMPLETED)).thenReturn(List.of(doc));
        var result = (List<McpToolProvider.DocumentInfo>) mcpToolProvider.getDocumentsByStatus("COMPLETED");
        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).status());
    }

    @Test
    void getLastUploadedDocument_returnsLatest() {
        Document doc = Document.builder().id("doc-1").name("latest.pdf").uploadedAt(LocalDateTime.now()).user(testUser).build();
        when(documentRepository.findFirstByUserOrderByUploadedAtDesc(testUser)).thenReturn(Optional.of(doc));
        var result = (McpToolProvider.DocumentInfo) mcpToolProvider.getLastUploadedDocument();
        assertNotNull(result);
        assertEquals("latest.pdf", result.name());
    }

    @Test
    void getLastUploadedDocument_returnsNullWhenEmpty() {
        when(documentRepository.findFirstByUserOrderByUploadedAtDesc(testUser)).thenReturn(Optional.empty());
        assertNull(mcpToolProvider.getLastUploadedDocument());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchDocuments_returnsMatches() {
        Document doc = Document.builder().id("doc-1").name("receipt.pdf").status(DocumentStatus.COMPLETED).user(testUser).build();
        when(documentRepository.findByUserAndNameContainingIgnoreCase(testUser, "receipt")).thenReturn(List.of(doc));
        var result = (List<McpToolProvider.DocumentInfo>) mcpToolProvider.searchDocuments("receipt");
        assertEquals(1, result.size());
    }

    // ==================== Document Write Tools ====================

    @Test
    void createDocument_savesAndReturns() {
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.DocumentCreateResponse) mcpToolProvider.createDocument("invoice.pdf", "application/pdf", 1024L, "RECEIVED", "BILL");
        assertNotNull(result.id());
        assertEquals("invoice.pdf", result.name());
        assertEquals("RECEIVED", result.status());
    }

    @Test
    void updateDocumentStatus_updatesAndReturns() {
        Document doc = Document.builder().id("doc-1").name("doc.pdf").status(DocumentStatus.RECEIVED).user(testUser).build();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.DocumentUpdateStatusResponse) mcpToolProvider.updateDocumentStatus("doc-1", "COMPLETED");
        assertEquals("COMPLETED", result.status());
    }

    @Test
    void deleteDocument_deletesAndReturns() {
        Document doc = Document.builder().id("doc-1").user(testUser).build();
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        var result = (McpToolProvider.DocumentDeleteResponse) mcpToolProvider.deleteDocument("doc-1");
        assertTrue(result.deleted());
        verify(documentRepository).delete(doc);
    }

    // ==================== Vendor Read Tools ====================

    @Test
    @SuppressWarnings("unchecked")
    void getVendors_returnsAll() {
        Vendor v = Vendor.builder().id("v-1").name("Fresh Farms").category("produce").user(testUser).build();
        when(vendorRepository.findByUser(testUser)).thenReturn(List.of(v));
        var result = (List<McpToolProvider.VendorInfo>) mcpToolProvider.getVendors();
        assertEquals(1, result.size());
        assertEquals("Fresh Farms", result.get(0).name());
    }

    @Test
    void getVendorById_returnsVendor() {
        Vendor v = Vendor.builder().id("v-1").name("Fresh Farms").user(testUser).build();
        when(vendorRepository.findById("v-1")).thenReturn(Optional.of(v));
        var result = (McpToolProvider.VendorInfo) mcpToolProvider.getVendorById("v-1");
        assertEquals("Fresh Farms", result.name());
    }

    @Test
    void getVendorById_throwsWhenWrongOwner() {
        User other = User.builder().id("user-2").build();
        Vendor v = Vendor.builder().id("v-1").user(other).build();
        when(vendorRepository.findById("v-1")).thenReturn(Optional.of(v));
        var result = mcpToolProvider.getVendorById("v-1");
        assertInstanceOf(Map.class, result);
        assertEquals("ACCESS_DENIED", ((Map<?, ?>) result).get("error"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getVendorsByCategory_returnsFiltered() {
        Vendor v = Vendor.builder().id("v-1").name("Dairy Co").category("dairy").user(testUser).build();
        when(vendorRepository.findByUserAndCategory(testUser, "dairy")).thenReturn(List.of(v));
        var result = (List<McpToolProvider.VendorInfo>) mcpToolProvider.getVendorsByCategory("dairy");
        assertEquals(1, result.size());
        assertEquals("dairy", result.get(0).category());
    }

    // ==================== Vendor Write Tools ====================

    @Test
    void createVendor_savesAndReturns() {
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.VendorCreateResponse) mcpToolProvider.createVendor("Fresh Farms", "produce", "555-0100");
        assertNotNull(result.id());
        assertEquals("Fresh Farms", result.name());
        assertEquals("produce", result.category());
    }

    @Test
    void updateVendor_updatesFields() {
        Vendor v = Vendor.builder().id("v-1").name("Old Name").category("old").contactInfo("old info").user(testUser).build();
        when(vendorRepository.findById("v-1")).thenReturn(Optional.of(v));
        when(vendorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.VendorUpdateResponse) mcpToolProvider.updateVendor("v-1", "New Name", "new cat", "new info");
        assertEquals("New Name", result.name());
        assertEquals("new cat", result.category());
        assertEquals("new info", result.contactInfo());
    }

    @Test
    void deleteVendor_deletesAndReturns() {
        Vendor v = Vendor.builder().id("v-1").user(testUser).build();
        when(vendorRepository.findById("v-1")).thenReturn(Optional.of(v));
        var result = (McpToolProvider.VendorDeleteResponse) mcpToolProvider.deleteVendor("v-1");
        assertTrue(result.deleted());
        verify(vendorRepository).delete(v);
    }

    // ==================== Inventory Read Tools ====================

    @Test
    @SuppressWarnings("unchecked")
    void getInventoryItems_returnsAll() {
        InventoryItem item = InventoryItem.builder().id("i-1").name("Tomatoes").currentStock(BigDecimal.TEN).unit("kg").user(testUser).build();
        when(inventoryItemRepository.findByUser(testUser)).thenReturn(List.of(item));
        var result = (List<McpToolProvider.InventoryItemInfo>) mcpToolProvider.getInventoryItems();
        assertEquals(1, result.size());
        assertEquals("Tomatoes", result.get(0).name());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLowStockItems_returnsLowStock() {
        InventoryItem item = InventoryItem.builder().id("i-1").name("Milk").currentStock(BigDecimal.valueOf(2)).minThreshold(BigDecimal.TEN).unit("L").user(testUser).build();
        when(inventoryItemRepository.findLowStockByUser(testUser)).thenReturn(List.of(item));
        var result = (List<McpToolProvider.InventoryItemInfo>) mcpToolProvider.getLowStockItems();
        assertEquals(1, result.size());
    }

    @Test
    void getInventoryItemById_returnsItem() {
        InventoryItem item = InventoryItem.builder().id("i-1").name("Tomatoes").currentStock(BigDecimal.TEN).unit("kg").user(testUser).build();
        when(inventoryItemRepository.findById("i-1")).thenReturn(Optional.of(item));
        var result = (McpToolProvider.InventoryItemInfo) mcpToolProvider.getInventoryItemById("i-1");
        assertEquals("Tomatoes", result.name());
    }

    @Test
    void getInventoryItemById_throwsWhenWrongOwner() {
        User other = User.builder().id("user-2").build();
        InventoryItem item = InventoryItem.builder().id("i-1").user(other).build();
        when(inventoryItemRepository.findById("i-1")).thenReturn(Optional.of(item));
        var result = mcpToolProvider.getInventoryItemById("i-1");
        assertInstanceOf(Map.class, result);
        assertEquals("ACCESS_DENIED", ((Map<?, ?>) result).get("error"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getInventoryByCategory_returnsFiltered() {
        InventoryItem item = InventoryItem.builder().id("i-1").name("Cheese").category("dairy").user(testUser).currentStock(BigDecimal.valueOf(5)).unit("kg").build();
        when(inventoryItemRepository.findByUserAndCategory(testUser, "dairy")).thenReturn(List.of(item));
        var result = (List<McpToolProvider.InventoryItemInfo>) mcpToolProvider.getInventoryByCategory("dairy");
        assertEquals(1, result.size());
    }

    // ==================== Inventory Write Tools ====================

    @Test
    void createInventoryItem_savesAndReturns() {
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.InventoryItemCreateResponse) mcpToolProvider.createInventoryItem("Tomatoes", 50.0, "kg", 2.5, 10.0, "produce");
        assertNotNull(result.id());
        assertEquals("Tomatoes", result.name());
    }

    @Test
    void updateStock_updatesAndReturns() {
        InventoryItem item = InventoryItem.builder().id("i-1").name("Tomatoes").currentStock(BigDecimal.TEN).user(testUser).build();
        when(inventoryItemRepository.findById("i-1")).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.InventoryItemUpdateStockResponse) mcpToolProvider.updateStock("i-1", 45.0);
        assertEquals(BigDecimal.TEN, result.previousStock());
        assertEquals(BigDecimal.valueOf(45.0), result.currentStock());
    }

    @Test
    void createStockTransaction_recordsTransaction() {
        InventoryItem item = InventoryItem.builder().id("i-1").name("Tomatoes").currentStock(BigDecimal.TEN).user(testUser).build();
        when(inventoryItemRepository.findById("i-1")).thenReturn(Optional.of(item));
        when(stockTransactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventoryItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.StockTransactionResponse) mcpToolProvider.createStockTransaction("i-1", 20.0, "PURCHASE", null);
        assertEquals("PURCHASE", result.type());
        assertEquals(BigDecimal.valueOf(30.0), result.newStock());
    }

    @Test
    void deleteInventoryItem_deletesAndReturns() {
        InventoryItem item = InventoryItem.builder().id("i-1").user(testUser).build();
        when(inventoryItemRepository.findById("i-1")).thenReturn(Optional.of(item));
        var result = (McpToolProvider.InventoryItemDeleteResponse) mcpToolProvider.deleteInventoryItem("i-1");
        assertTrue(result.deleted());
        verify(inventoryItemRepository).delete(item);
    }

    // ==================== Order Read Tools ====================

    @Test
    @SuppressWarnings("unchecked")
    void getRecentOrders_returnsOrders() {
        Order order = Order.builder().id("o-1").orderDate(LocalDateTime.now()).totalAmount(BigDecimal.valueOf(25.50)).status("PENDING").restaurant(testRestaurant).items(List.of()).build();
        when(orderRepository.findTop10ByRestaurantOrderByOrderDateDesc(testRestaurant)).thenReturn(List.of(order));
        var result = (List<McpToolProvider.OrderSummaryInfo>) mcpToolProvider.getRecentOrders();
        assertEquals(1, result.size());
        assertEquals("o-1", result.get(0).id());
    }

    @Test
    void getOrderById_returnsOrderDetail() {
        MenuItem mi = MenuItem.builder().id("mi-1").name("Burger").price(BigDecimal.valueOf(10)).build();
        OrderItem oi = OrderItem.builder().id("oi-1").menuItem(mi).quantity(2).price(BigDecimal.valueOf(10)).build();
        Order order = Order.builder().id("o-1").orderDate(LocalDateTime.now()).totalAmount(BigDecimal.valueOf(20)).status("PENDING").restaurant(testRestaurant).items(List.of(oi)).build();
        when(orderRepository.findByIdWithItems("o-1")).thenReturn(Optional.of(order));
        var result = (McpToolProvider.OrderDetailInfo) mcpToolProvider.getOrderById("o-1");
        assertEquals("o-1", result.id());
        assertEquals(1, result.items().size());
        assertEquals("Burger", result.items().get(0).menuItemName());
    }

    @Test
    void getOrderById_throwsWhenWrongRestaurant() {
        Restaurant other = Restaurant.builder().id("rest-2").build();
        Order order = Order.builder().id("o-1").restaurant(other).build();
        when(orderRepository.findByIdWithItems("o-1")).thenReturn(Optional.of(order));
        var result = mcpToolProvider.getOrderById("o-1");
        assertInstanceOf(Map.class, result);
        assertEquals("ACCESS_DENIED", ((Map<?, ?>) result).get("error"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrdersByDateRange_returnsOrders() {
        Order order = Order.builder().id("o-1").orderDate(LocalDateTime.now()).totalAmount(BigDecimal.TEN).status("COMPLETED").restaurant(testRestaurant).items(List.of()).build();
        when(orderRepository.findByRestaurantAndOrderDateBetween(eq(testRestaurant), any(), any())).thenReturn(List.of(order));
        var result = (List<McpToolProvider.OrderSummaryInfo>) mcpToolProvider.getOrdersByDateRange("2026-05-01", "2026-05-31");
        assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getOrdersByStatus_returnsFiltered() {
        Order order = Order.builder().id("o-1").orderDate(LocalDateTime.now()).totalAmount(BigDecimal.TEN).status("PENDING").restaurant(testRestaurant).items(List.of()).build();
        when(orderRepository.findByRestaurantAndStatus(testRestaurant, "PENDING")).thenReturn(List.of(order));
        var result = (List<McpToolProvider.OrderSummaryInfo>) mcpToolProvider.getOrdersByStatus("PENDING");
        assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getRecentOrders_handlesNullItemsGracefully() {
        Order order = Order.builder().id("o-1").orderDate(LocalDateTime.now()).totalAmount(BigDecimal.valueOf(25.50)).status("PENDING").restaurant(testRestaurant).items(null).build();
        when(orderRepository.findTop10ByRestaurantOrderByOrderDateDesc(testRestaurant)).thenReturn(List.of(order));
        var result = (List<McpToolProvider.OrderSummaryInfo>) mcpToolProvider.getRecentOrders();
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).itemCount());
    }

    @Test
    void getOrderById_handlesNullItemsGracefully() {
        Order order = Order.builder().id("o-1").orderDate(LocalDateTime.now()).totalAmount(BigDecimal.valueOf(20)).status("PENDING").restaurant(testRestaurant).items(null).build();
        when(orderRepository.findByIdWithItems("o-1")).thenReturn(Optional.of(order));
        var result = (McpToolProvider.OrderDetailInfo) mcpToolProvider.getOrderById("o-1");
        assertEquals("o-1", result.id());
        assertEquals(0, result.items().size());
    }

    // ==================== Order Write Tools ====================

    @Test
    void createOrder_savesAndReturns() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.OrderCreateResponse) mcpToolProvider.createOrder(25.0, "PENDING", null);
        assertNotNull(result.id());
        assertEquals("PENDING", result.status());
    }

    @Test
    void createOrder_withItemsComputesTotal() {
        MenuItem mi = MenuItem.builder().id("mi-1").name("Burger").price(BigDecimal.valueOf(10)).build();
        when(menuItemRepository.findById("mi-1")).thenReturn(Optional.of(mi));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        var items = List.of(new McpToolProvider.OrderItemInput("mi-1", 2, 10.0));
        var result = (McpToolProvider.OrderCreateResponse) mcpToolProvider.createOrder(0.0, "PENDING", items);
        assertEquals(BigDecimal.valueOf(20), result.totalAmount());
        assertEquals(1, result.itemCount());
    }

    @Test
    void updateOrderStatus_updatesAndReturns() {
        Order order = Order.builder().id("o-1").status("PENDING").orderDate(LocalDateTime.now()).restaurant(testRestaurant).build();
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.OrderStatusUpdateResponse) mcpToolProvider.updateOrderStatus("o-1", "COMPLETED");
        assertEquals("COMPLETED", result.status());
    }

    @Test
    void addOrderItem_addsAndRecalculates() {
        MenuItem mi = MenuItem.builder().id("mi-1").name("Fries").price(BigDecimal.valueOf(5)).build();
        Order order = Order.builder().id("o-1").restaurant(testRestaurant).items(new java.util.ArrayList<>()).totalAmount(BigDecimal.ZERO).build();
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(order));
        when(menuItemRepository.findById("mi-1")).thenReturn(Optional.of(mi));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.OrderItemAddResponse) mcpToolProvider.addOrderItem("o-1", "mi-1", 3, 5.0);
        assertEquals("o-1", result.orderId());
        assertNotNull(result.orderItemId());
    }

    // ==================== Menu Read Tools ====================

    @Test
    @SuppressWarnings("unchecked")
    void getMenuItems_returnsAll() {
        MenuItem mi = MenuItem.builder().id("mi-1").name("Burger").price(BigDecimal.TEN).category("entrees").restaurant(testRestaurant).build();
        when(menuItemRepository.findByRestaurant(testRestaurant)).thenReturn(List.of(mi));
        var result = (List<McpToolProvider.MenuItemInfo>) mcpToolProvider.getMenuItems();
        assertEquals(1, result.size());
        assertEquals("Burger", result.get(0).name());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMenuItemsByCategory_returnsFiltered() {
        MenuItem mi = MenuItem.builder().id("mi-1").name("Cake").price(BigDecimal.valueOf(8)).category("desserts").restaurant(testRestaurant).build();
        when(menuItemRepository.findByRestaurantAndCategory(testRestaurant, "desserts")).thenReturn(List.of(mi));
        var result = (List<McpToolProvider.MenuItemInfo>) mcpToolProvider.getMenuItemsByCategory("desserts");
        assertEquals(1, result.size());
    }

    // ==================== Metrics Read Tools ====================

    @Test
    @SuppressWarnings("unchecked")
    void getRestaurantMetrics_returnsMetrics() {
        RestaurantMetric metric = RestaurantMetric.builder().id("m-1").metricName("revenue").metricValue(BigDecimal.valueOf(5000)).periodStart(LocalDateTime.now().minusDays(30)).periodEnd(LocalDateTime.now()).restaurant(testRestaurant).build();
        when(restaurantMetricRepository.findByRestaurant(testRestaurant)).thenReturn(List.of(metric));
        var result = (List<McpToolProvider.RestaurantMetricInfo>) mcpToolProvider.getRestaurantMetrics();
        assertEquals(1, result.size());
        assertEquals("revenue", result.get(0).metricName());
    }

    // ==================== Subscription Read Tool ====================

    @Test
    void getSubscriptionInfo_returnsPlanAndUsage() {
        SubscriptionPlan plan = SubscriptionPlan.builder().id("p-1").name("Pro").price(BigDecimal.valueOf(29.99)).maxDocumentsSizeMb(500L).maxChatsPerMonth(100).maxAccountsPerRestaurant(10).maxNotificationsPerMonth(50).build();
        RestaurantSubscription sub = RestaurantSubscription.builder().id("rs-1").restaurant(testRestaurant).plan(plan).startDate(LocalDateTime.now().minusMonths(1)).endDate(LocalDateTime.now().plusMonths(11)).build();
        SubscriptionUsage usage = SubscriptionUsage.builder().id("u-1").restaurant(testRestaurant).monthYear("2026-06").currentDocumentsSizeBytes(104857600L).chatsCount(25).notificationsCount(5).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.of(usage));
        var result = (McpToolProvider.SubscriptionInfo) mcpToolProvider.getSubscriptionInfo();
        assertEquals("Pro", result.planName());
        assertEquals(100, result.maxChatsPerMonth());
        assertEquals(25, result.currentChatsCount());
    }

    @Test
    void getSubscriptionInfo_handlesNoSubscription() {
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.empty());
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.empty());
        var result = (McpToolProvider.SubscriptionInfo) mcpToolProvider.getSubscriptionInfo();
        assertNull(result.planName());
    }

    // ==================== Subscription Write Tools ====================

    @Test
    void checkSubscriptionLimit_chatAllowed() {
        SubscriptionPlan plan = SubscriptionPlan.builder().id("p-1").maxChatsPerMonth(100).build();
        RestaurantSubscription sub = RestaurantSubscription.builder().restaurant(testRestaurant).plan(plan).build();
        SubscriptionUsage usage = SubscriptionUsage.builder().restaurant(testRestaurant).monthYear("2026-06").chatsCount(50).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.of(usage));
        var result = (McpToolProvider.SubscriptionLimitCheckResponse) mcpToolProvider.checkSubscriptionLimit("CHAT");
        assertTrue(result.allowed());
        assertEquals(50, result.currentUsage());
        assertEquals(100, result.limit());
        assertEquals(50, result.remaining());
    }

    @Test
    void checkSubscriptionLimit_chatDenied() {
        SubscriptionPlan plan = SubscriptionPlan.builder().id("p-1").maxChatsPerMonth(100).build();
        RestaurantSubscription sub = RestaurantSubscription.builder().restaurant(testRestaurant).plan(plan).build();
        SubscriptionUsage usage = SubscriptionUsage.builder().restaurant(testRestaurant).monthYear("2026-06").chatsCount(100).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.of(usage));
        var result = (McpToolProvider.SubscriptionLimitCheckResponse) mcpToolProvider.checkSubscriptionLimit("CHAT");
        assertFalse(result.allowed());
        assertEquals(0, result.remaining());
    }

    @Test
    void checkSubscriptionLimit_noSubscription() {
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.empty());
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.empty());
        var result = (McpToolProvider.SubscriptionLimitCheckResponse) mcpToolProvider.checkSubscriptionLimit("CHAT");
        assertFalse(result.allowed());
    }

    @Test
    void incrementUsage_chatIncrements() {
        RestaurantSubscription sub = RestaurantSubscription.builder().restaurant(testRestaurant).build();
        SubscriptionUsage usage = SubscriptionUsage.builder().id("u-1").restaurant(testRestaurant).monthYear("2026-06").chatsCount(10).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(eq(testRestaurant), anyString())).thenReturn(Optional.of(usage));
        when(subscriptionUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.SubscriptionUsageIncrementResponse) mcpToolProvider.incrementUsage("CHAT", null);
        assertEquals(11, result.newCount());
        assertEquals("2026-06", result.monthYear());
    }

    @Test
    void incrementUsage_createsUsageIfNotExist() {
        RestaurantSubscription sub = RestaurantSubscription.builder().restaurant(testRestaurant).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(testRestaurant, "2026-05")).thenReturn(Optional.empty());
        when(subscriptionUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.SubscriptionUsageIncrementResponse) mcpToolProvider.incrementUsage("CHAT", null);
        assertEquals(1, result.newCount());
    }

    @Test
    void incrementUsage_documentUpload_bytesToMb() {
        RestaurantSubscription sub = RestaurantSubscription.builder().restaurant(testRestaurant).build();
        SubscriptionUsage usage = SubscriptionUsage.builder().id("u-1").restaurant(testRestaurant).monthYear("2026-05").currentDocumentsSizeBytes(0L).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(testRestaurant, "2026-05")).thenReturn(Optional.of(usage));
        when(subscriptionUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = (McpToolProvider.SubscriptionUsageIncrementResponse) mcpToolProvider.incrementUsage("DOCUMENT_UPLOAD", 5242880);
        assertEquals(5, result.newCount());
    }

    @Test
    void incrementUsage_unknownType_returnsValidation() {
        RestaurantSubscription sub = RestaurantSubscription.builder().restaurant(testRestaurant).build();
        when(restaurantSubscriptionRepository.findByRestaurant(testRestaurant)).thenReturn(Optional.of(sub));
        when(subscriptionUsageRepository.findByRestaurantAndMonthYear(testRestaurant, "2026-05")).thenReturn(Optional.empty());
        when(subscriptionUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var result = mcpToolProvider.incrementUsage("UNKNOWN", null);
        assertInstanceOf(Map.class, result);
        assertEquals("VALIDATION_ERROR", ((Map<?, ?>) result).get("error"));
    }
}
