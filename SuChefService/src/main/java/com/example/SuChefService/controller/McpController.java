package com.example.SuChefService.controller;

import com.example.SuChefService.mcp.McpToolProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpToolProvider mcpToolProvider;

    private ResponseEntity<Object> handleResult(Object result) {
        if (result instanceof Map<?, ?> map && map.containsKey("error")) {
            String errorType = (String) map.get("error");
            return switch (errorType) {
                case "NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
                case "ACCESS_DENIED" -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
                case "VALIDATION_ERROR" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
                case "RATE_LIMITED" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            };
        }
        return ResponseEntity.ok(result);
    }

    // ===================== Documents =====================

    @GetMapping("/documents")
    public ResponseEntity<Object> getUserDocuments() {
        return handleResult(mcpToolProvider.getUserDocuments());
    }

    @GetMapping("/documents/count")
    public ResponseEntity<Object> getDocumentCount() {
        return handleResult(mcpToolProvider.getDocumentCount());
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<Object> getDocumentById(@PathVariable String id) {
        return handleResult(mcpToolProvider.getDocumentById(id));
    }

    @GetMapping("/documents/status/{status}")
    public ResponseEntity<Object> getDocumentsByStatus(@PathVariable String status) {
        return handleResult(mcpToolProvider.getDocumentsByStatus(status));
    }

    @GetMapping("/documents/last")
    public ResponseEntity<Object> getLastUploadedDocument() {
        return handleResult(mcpToolProvider.getLastUploadedDocument());
    }

    @GetMapping("/documents/search")
    public ResponseEntity<Object> searchDocuments(@RequestParam String name) {
        return handleResult(mcpToolProvider.searchDocuments(name));
    }

    @PostMapping("/documents")
    public ResponseEntity<Object> createDocument(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String type = (String) body.get("type");
        Long size = body.get("size") != null ? ((Number) body.get("size")).longValue() : null;
        String status = (String) body.get("status");
        String classification = (String) body.get("classification");
        return handleResult(mcpToolProvider.createDocument(name, type, size, status, classification));
    }

    @PutMapping("/documents/{id}/status")
    public ResponseEntity<Object> updateDocumentStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return handleResult(mcpToolProvider.updateDocumentStatus(id, status));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Object> deleteDocument(@PathVariable String id) {
        return handleResult(mcpToolProvider.deleteDocument(id));
    }

    // ===================== Vendors =====================

    @GetMapping("/vendors")
    public ResponseEntity<Object> getVendors() {
        return handleResult(mcpToolProvider.getVendors());
    }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<Object> getVendorById(@PathVariable String id) {
        return handleResult(mcpToolProvider.getVendorById(id));
    }

    @GetMapping("/vendors/category/{category}")
    public ResponseEntity<Object> getVendorsByCategory(@PathVariable String category) {
        return handleResult(mcpToolProvider.getVendorsByCategory(category));
    }

    @PostMapping("/vendors")
    public ResponseEntity<Object> createVendor(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String category = body.get("category");
        String contactInfo = body.get("contactInfo");
        return handleResult(mcpToolProvider.createVendor(name, category, contactInfo));
    }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<Object> updateVendor(@PathVariable String id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String category = body.get("category");
        String contactInfo = body.get("contactInfo");
        return handleResult(mcpToolProvider.updateVendor(id, name, category, contactInfo));
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Object> deleteVendor(@PathVariable String id) {
        return handleResult(mcpToolProvider.deleteVendor(id));
    }

    // ===================== Inventory =====================

    @GetMapping("/inventory")
    public ResponseEntity<Object> getInventoryItems() {
        return handleResult(mcpToolProvider.getInventoryItems());
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<Object> getLowStockItems() {
        return handleResult(mcpToolProvider.getLowStockItems());
    }

    @GetMapping("/inventory/{id}")
    public ResponseEntity<Object> getInventoryItemById(@PathVariable String id) {
        return handleResult(mcpToolProvider.getInventoryItemById(id));
    }

    @GetMapping("/inventory/category/{category}")
    public ResponseEntity<Object> getInventoryByCategory(@PathVariable String category) {
        return handleResult(mcpToolProvider.getInventoryByCategory(category));
    }

    @PostMapping("/inventory")
    public ResponseEntity<Object> createInventoryItem(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        double currentStock = body.get("currentStock") != null ? ((Number) body.get("currentStock")).doubleValue() : 0.0;
        String unit = (String) body.get("unit");
        Double unitPrice = body.get("unitPrice") != null ? ((Number) body.get("unitPrice")).doubleValue() : null;
        Double minThreshold = body.get("minThreshold") != null ? ((Number) body.get("minThreshold")).doubleValue() : null;
        String category = (String) body.get("category");
        return handleResult(mcpToolProvider.createInventoryItem(name, currentStock, unit, unitPrice, minThreshold, category));
    }

    @PutMapping("/inventory/{id}/stock")
    public ResponseEntity<Object> updateStock(@PathVariable String id, @RequestBody Map<String, Object> body) {
        double newStock = body.get("newStock") != null ? ((Number) body.get("newStock")).doubleValue() : 0.0;
        return handleResult(mcpToolProvider.updateStock(id, newStock));
    }

    @PostMapping("/inventory/transactions")
    public ResponseEntity<Object> createStockTransaction(@RequestBody Map<String, Object> body) {
        String itemId = (String) body.get("itemId");
        double quantityChange = body.get("quantityChange") != null ? ((Number) body.get("quantityChange")).doubleValue() : 0.0;
        String type = (String) body.get("type");
        String documentId = (String) body.get("documentId");
        return handleResult(mcpToolProvider.createStockTransaction(itemId, quantityChange, type, documentId));
    }

    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<Object> deleteInventoryItem(@PathVariable String id) {
        return handleResult(mcpToolProvider.deleteInventoryItem(id));
    }

    // ===================== Orders =====================

    @GetMapping("/orders/recent")
    public ResponseEntity<Object> getRecentOrders() {
        return handleResult(mcpToolProvider.getRecentOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable String id) {
        return handleResult(mcpToolProvider.getOrderById(id));
    }

    @GetMapping("/orders/date-range")
    public ResponseEntity<Object> getOrdersByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        return handleResult(mcpToolProvider.getOrdersByDateRange(startDate, endDate));
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<Object> getOrdersByStatus(@PathVariable String status) {
        return handleResult(mcpToolProvider.getOrdersByStatus(status));
    }

    @PostMapping("/orders")
    public ResponseEntity<Object> createOrder(@RequestBody Map<String, Object> body) {
        double totalAmount = body.get("totalAmount") != null ? ((Number) body.get("totalAmount")).doubleValue() : 0.0;
        String status = (String) body.get("status");
        @SuppressWarnings("unchecked")
        List<McpToolProvider.OrderItemInput> items = null;
        if (body.get("items") instanceof List<?> rawItems) {
            items = rawItems.stream()
                    .filter(McpToolProvider.OrderItemInput.class::isInstance)
                    .map(McpToolProvider.OrderItemInput.class::cast)
                    .toList();
        }
        return handleResult(mcpToolProvider.createOrder(totalAmount, status, items));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Object> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return handleResult(mcpToolProvider.updateOrderStatus(id, status));
    }

    @PostMapping("/orders/{id}/items")
    public ResponseEntity<Object> addOrderItem(@PathVariable String id, @RequestBody Map<String, Object> body) {
        String menuItemId = (String) body.get("menuItemId");
        int quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
        double price = body.get("price") != null ? ((Number) body.get("price")).doubleValue() : 0.0;
        return handleResult(mcpToolProvider.addOrderItem(id, menuItemId, quantity, price));
    }

    // ===================== Menu =====================

    @GetMapping("/menu")
    public ResponseEntity<Object> getMenuItems() {
        return handleResult(mcpToolProvider.getMenuItems());
    }

    @GetMapping("/menu/category/{category}")
    public ResponseEntity<Object> getMenuItemsByCategory(@PathVariable String category) {
        return handleResult(mcpToolProvider.getMenuItemsByCategory(category));
    }

    // ===================== Metrics =====================

    @GetMapping("/metrics")
    public ResponseEntity<Object> getRestaurantMetrics() {
        return handleResult(mcpToolProvider.getRestaurantMetrics());
    }

    // ===================== Subscription =====================

    @GetMapping("/subscription/info")
    public ResponseEntity<Object> getSubscriptionInfo() {
        return handleResult(mcpToolProvider.getSubscriptionInfo());
    }

    @PostMapping("/subscription/check-limit")
    public ResponseEntity<Object> checkSubscriptionLimit(@RequestParam String limitType) {
        return handleResult(mcpToolProvider.checkSubscriptionLimit(limitType));
    }

    @PostMapping("/subscription/increment-usage")
    public ResponseEntity<Object> incrementUsage(@RequestBody Map<String, Object> body) {
        String actionType = (String) body.get("actionType");
        Integer amount = body.get("amount") != null ? ((Number) body.get("amount")).intValue() : null;
        return handleResult(mcpToolProvider.incrementUsage(actionType, amount));
    }

    @GetMapping("/subscription/plans")
    public ResponseEntity<Object> getAllPlans() {
        return handleResult(mcpToolProvider.getAllPlans());
    }

    @GetMapping("/subscription/account-count")
    public ResponseEntity<Object> getAccountCount() {
        return handleResult(mcpToolProvider.getAccountCount());
    }
}
