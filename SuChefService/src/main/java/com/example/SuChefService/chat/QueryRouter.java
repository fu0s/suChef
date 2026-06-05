package com.example.SuChefService.chat;

import com.example.SuChefService.entity.*;
import com.example.SuChefService.repository.*;
import com.example.SuChefService.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryRouter {

    private final DocumentRepository documentRepository;
    private final OrderRepository orderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final VendorRepository vendorRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantMetricRepository restaurantMetricRepository;
    private final RestaurantSubscriptionRepository restaurantSubscriptionRepository;
    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Transactional(readOnly = true)
    public QueryResult execute(IntentClassifier.ClassifiedIntent intent) {
        try {
            return switch (intent.intent()) {
                case RESTAURANT_NAME -> {
                    Restaurant r = subscriptionService.getCurrentRestaurant();
                    yield new QueryResult("restaurant_name", Map.of("name", r.getName()));
                }
                case DOCUMENT_COUNT -> {
                    User u = getCurrentUser();
                    long count = documentRepository.countByUser(u);
                    yield new QueryResult("document_count", Map.of("count", count));
                }
                case DOCUMENT_LIST -> {
                    User u = getCurrentUser();
                    var docs = documentRepository.findByUser(u);
                    yield new QueryResult("document_list", Map.of(
                            "count", docs.size(),
                            "documents", docs.stream().map(d -> Map.of(
                                    "id", d.getId(),
                                    "name", d.getName(),
                                    "type", d.getType() != null ? d.getType() : "",
                                    "status", d.getStatus() != null ? d.getStatus().name() : "",
                                    "classification", d.getClassification() != null ? d.getClassification().name() : ""
                            )).toList()
                    ));
                }
                case DOCUMENT_LATEST -> {
                    User u = getCurrentUser();
                    var doc = documentRepository.findFirstByUserOrderByUploadedAtDesc(u);
                    if (doc.isPresent()) {
                        var d = doc.get();
                        yield new QueryResult("document_latest", Map.of(
                                "id", d.getId(), "name", d.getName(),
                                "status", d.getStatus() != null ? d.getStatus().name() : "",
                                "uploadedAt", d.getUploadedAt() != null ? d.getUploadedAt().toString() : ""
                        ));
                    }
                    yield new QueryResult("document_latest", Map.of("found", false));
                }
                case ORDER_RECENT -> {
                    Restaurant r = subscriptionService.getCurrentRestaurant();
                    var orders = orderRepository.findTop10ByRestaurantOrderByOrderDateDesc(r);
                    yield new QueryResult("order_list", Map.of(
                            "count", orders.size(),
                            "orders", orders.stream().map(o -> Map.of(
                                    "id", o.getId(),
                                    "date", o.getOrderDate() != null ? o.getOrderDate().toString() : "",
                                    "total", o.getTotalAmount(),
                                    "status", o.getStatus()
                            )).toList()
                    ));
                }
                case ORDER_BY_STATUS -> {
                    Restaurant r = subscriptionService.getCurrentRestaurant();
                    String status = intent.filter() != null ? intent.filter() : "PENDING";
                    var orders = orderRepository.findByRestaurantAndStatus(r, status);
                    yield new QueryResult("order_list", Map.of(
                            "status", status,
                            "count", orders.size(),
                            "orders", orders.stream().map(o -> Map.of(
                                    "id", o.getId(),
                                    "date", o.getOrderDate() != null ? o.getOrderDate().toString() : "",
                                    "total", o.getTotalAmount(),
                                    "status", o.getStatus()
                            )).toList()
                    ));
                }
                case ORDER_COUNT -> {
                    Restaurant r = subscriptionService.getCurrentRestaurant();
                    long count = orderRepository.findByRestaurant(r).size();
                    yield new QueryResult("order_count", Map.of("count", count));
                }
                case INVENTORY_LIST -> {
                    User u = getCurrentUser();
                    var items = inventoryItemRepository.findByUser(u);
                    yield new QueryResult("inventory_list", Map.of(
                            "count", items.size(),
                            "items", items.stream().map(i -> Map.of(
                                    "id", i.getId(),
                                    "name", i.getName(),
                                    "stock", i.getCurrentStock(),
                                    "unit", i.getUnit() != null ? i.getUnit() : "",
                                    "category", i.getCategory() != null ? i.getCategory() : ""
                            )).toList()
                    ));
                }
                case INVENTORY_LOW_STOCK -> {
                    User u = getCurrentUser();
                    var items = inventoryItemRepository.findLowStockByUser(u);
                    yield new QueryResult("low_stock_list", Map.of(
                            "count", items.size(),
                            "items", items.stream().map(i -> Map.of(
                                    "id", i.getId(),
                                    "name", i.getName(),
                                    "stock", i.getCurrentStock(),
                                    "minThreshold", i.getMinThreshold(),
                                    "unit", i.getUnit() != null ? i.getUnit() : ""
                            )).toList()
                    ));
                }
                case VENDOR_LIST -> {
                    User u = getCurrentUser();
                    var vendors = vendorRepository.findByUser(u);
                    yield new QueryResult("vendor_list", Map.of(
                            "count", vendors.size(),
                            "vendors", vendors.stream().map(v -> Map.of(
                                    "id", v.getId(),
                                    "name", v.getName(),
                                    "category", v.getCategory() != null ? v.getCategory() : ""
                            )).toList()
                    ));
                }
                case MENU_LIST -> {
                    Restaurant r = subscriptionService.getCurrentRestaurant();
                    var items = menuItemRepository.findByRestaurant(r);
                    yield new QueryResult("menu_list", Map.of(
                            "count", items.size(),
                            "items", items.stream().map(m -> Map.of(
                                    "id", m.getId(),
                                    "name", m.getName(),
                                    "price", m.getPrice(),
                                    "category", m.getCategory() != null ? m.getCategory() : ""
                            )).toList()
                    ));
                }
                case RESTAURANT_METRICS -> {
                    Restaurant r = subscriptionService.getCurrentRestaurant();
                    var metrics = restaurantMetricRepository.findByRestaurant(r);
                    yield new QueryResult("metrics", Map.of(
                            "count", metrics.size(),
                            "metrics", metrics.stream().map(m -> Map.of(
                                    "name", m.getMetricName(),
                                    "value", m.getMetricValue()
                            )).toList()
                    ));
                }
                case SUBSCRIPTION_INFO -> {
                    Restaurant r = subscriptionService.getCurrentRestaurant();
                    var sub = restaurantSubscriptionRepository.findByRestaurant(r).orElse(null);
                    var plan = sub != null ? sub.getPlan() : null;
                    String monthYear = YearMonth.now().toString();
                    var usage = subscriptionUsageRepository.findByRestaurantAndMonthYear(r, monthYear).orElse(null);
                    yield new QueryResult("subscription", Map.of(
                            "planName", plan != null ? plan.getName() : "none",
                            "planPrice", plan != null ? plan.getPrice() : 0,
                            "chatsUsed", usage != null && usage.getChatsCount() != null ? usage.getChatsCount() : 0,
                            "chatsLimit", plan != null && plan.getMaxChatsPerMonth() != null ? plan.getMaxChatsPerMonth() : 0,
                            "documentsMbUsed", usage != null && usage.getCurrentDocumentsSizeBytes() != null
                                    ? usage.getCurrentDocumentsSizeBytes() / (1024.0 * 1024.0) : 0.0,
                            "documentsMbLimit", plan != null && plan.getMaxDocumentsSizeMb() != null ? plan.getMaxDocumentsSizeMb() : 0
                    ));
                }
                default -> new QueryResult("general", Map.of());
            };
        } catch (Exception e) {
            log.error("QueryRouter error for intent {}: {}", intent.intent(), e.getMessage());
            return new QueryResult("error", Map.of("message", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof org.springframework.security.core.userdetails.UserDetails)
                ? ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername()
                : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.example.SuChefService.exception.ResourceNotFoundException("User not found"));
    }

    public record QueryResult(String type, Map<String, Object> data) {}
}
