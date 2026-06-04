package com.example.SuChefService.mcp;

import com.example.SuChefService.entity.Document;
import com.example.SuChefService.entity.DocumentClassification;
import com.example.SuChefService.entity.DocumentStatus;
import com.example.SuChefService.entity.InventoryItem;
import com.example.SuChefService.entity.MenuItem;
import com.example.SuChefService.entity.Order;
import com.example.SuChefService.entity.OrderItem;
import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.RestaurantMetric;
import com.example.SuChefService.entity.StockTransaction;
import com.example.SuChefService.entity.StockTransactionType;
import com.example.SuChefService.entity.SubscriptionUsage;
import com.example.SuChefService.entity.User;
import com.example.SuChefService.entity.Vendor;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpToolProvider {

    private final DocumentRepository documentRepository;
    private final VendorRepository vendorRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantMetricRepository restaurantMetricRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final RestaurantSubscriptionRepository restaurantSubscriptionRepository;
    private final SubscriptionUsageRepository subscriptionUsageRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    // --- User/Restaurant Resolution (public for service delegation) ---

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Restaurant getCurrentRestaurant() {
        User user = getCurrentUser();
        Restaurant restaurant = user.getRestaurant();
        if (restaurant == null) {
            throw new ResourceNotFoundException("No restaurant associated with the current user");
        }
        return restaurant;
    }

    // --- Structured Error Response Helpers (T-8.2) ---

    private Map<String, Object> notFoundError(String resource, String id) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "NOT_FOUND");
        error.put("resource", resource);
        if (id != null) error.put("id", id);
        error.put("message", resource + " not found" + (id != null ? " with id: " + id : ""));
        log.warn("MCP tool NOT_FOUND: {} id={}", resource, id);
        return error;
    }

    private Map<String, Object> accessDeniedError(String resource, String id) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "ACCESS_DENIED");
        error.put("resource", resource);
        if (id != null) error.put("id", id);
        error.put("message", "Access denied — " + resource.toLowerCase() + " belongs to another user");
        log.warn("MCP tool ACCESS_DENIED: {} id={}", resource, id);
        return error;
    }

    private Map<String, Object> validationError(String field, String message, Object... details) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "VALIDATION_ERROR");
        error.put("field", field);
        error.put("message", message);
        if (details.length > 0) error.put("details", details);
        log.warn("MCP tool VALIDATION_ERROR: {} — {}", field, message);
        return error;
    }

    // --- Document Tool DTOs ---

    public record DocumentCountResponse(long count) {}

    public record DocumentInfo(
            String id,
            String name,
            String type,
            Long size,
            LocalDateTime date,
            LocalDateTime uploadedAt,
            String status,
            String classification
    ) {}

    // --- Document Read Tools (T-D1 through T-D5) ---

    @Tool(description = "Get the total number of documents uploaded by the current user. Use this to answer 'how many documents' or 'document count' questions.")
    public Object getDocumentCount() {
        try {
            log.debug("MCP Tool getDocumentCount called");
            User user = getCurrentUser();
            long count = documentRepository.countByUser(user);
            log.debug("Returning document count: {}", count);
            return new DocumentCountResponse(count);
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getDocumentCount error: {}", e.getMessage());
            return validationError("getDocumentCount", e.getMessage());
        }
    }

    @Tool(description = "Get details of a specific document by its ID. Verifies the document belongs to the current user.")
    public Object getDocumentById(
            @ToolParam(description = "The document ID to retrieve") String documentId
    ) {
        try {
            log.debug("MCP Tool getDocumentById called with id: {}", documentId);
            User user = getCurrentUser();
            Document doc = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
            if (!doc.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Document", documentId);
            }
            return mapToDocumentInfo(doc);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Document", documentId);
        } catch (Exception e) {
            log.error("MCP Tool getDocumentById error: {}", e.getMessage());
            return validationError("getDocumentById", e.getMessage());
        }
    }

    @Tool(description = "Get all documents filtered by status. Valid statuses: RECEIVED, PROCESSING, OCR_PROCESSING, COMPLETED, FAILED.")
    public Object getDocumentsByStatus(
            @ToolParam(description = "Document status to filter by") String status
    ) {
        try {
            log.debug("MCP Tool getDocumentsByStatus called with status: {}", status);
            User user = getCurrentUser();
            DocumentStatus docStatus = DocumentStatus.valueOf(status.toUpperCase());
            return documentRepository.findByUserAndStatus(user, docStatus).stream()
                    .map(this::mapToDocumentInfo)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return validationError("status", "Invalid status value. Valid: RECEIVED, PROCESSING, OCR_PROCESSING, COMPLETED, FAILED", status);
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getDocumentsByStatus error: {}", e.getMessage());
            return validationError("getDocumentsByStatus", e.getMessage());
        }
    }

    @Tool(description = "Get the most recently uploaded document for the current user. Returns null if no documents exist.")
    public Object getLastUploadedDocument() {
        try {
            log.debug("MCP Tool getLastUploadedDocument called");
            User user = getCurrentUser();
            return documentRepository.findFirstByUserOrderByUploadedAtDesc(user)
                    .map(this::mapToDocumentInfo)
                    .orElse(null);
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getLastUploadedDocument error: {}", e.getMessage());
            return validationError("getLastUploadedDocument", e.getMessage());
        }
    }

    @Tool(description = "Search documents by name substring (case-insensitive). Use this to find documents matching a keyword like 'receipt', 'invoice', etc.")
    public Object searchDocuments(
            @ToolParam(description = "Name substring to search for (case-insensitive)") String nameQuery
    ) {
        try {
            log.debug("MCP Tool searchDocuments called with query: {}", nameQuery);
            User user = getCurrentUser();
            return documentRepository.findByUserAndNameContainingIgnoreCase(user, nameQuery).stream()
                    .map(this::mapToDocumentInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool searchDocuments error: {}", e.getMessage());
            return validationError("searchDocuments", e.getMessage());
        }
    }

    // --- Document Write Tool DTOs ---

    public record DocumentCreateResponse(String id, String name, String status) {}

    public record DocumentUpdateStatusResponse(String id, String name, String status) {}

    public record DocumentDeleteResponse(boolean deleted, String documentId) {}

    // --- Document Write Tools (T-D6 through T-D8) ---

    @Tool(description = "Store a new document record in the database. At minimum, provide name and status. Optionally provide type, size, and classification (BILL, ORDER, MENU). The document is assigned to the current user.")
    public Object createDocument(
            @ToolParam(description = "Document file name") String name,
            @ToolParam(description = "MIME type (e.g., application/pdf)", required = false) String type,
            @ToolParam(description = "File size in bytes", required = false) Long size,
            @ToolParam(description = "Initial document status: RECEIVED, PROCESSING, COMPLETED, or FAILED") String status,
            @ToolParam(description = "Document classification: BILL, ORDER, or MENU", required = false) String classification
    ) {
        try {
            log.debug("MCP Tool createDocument called with name: {}", name);
            User user = getCurrentUser();
            Document doc = Document.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .name(name)
                    .type(type)
                    .size(size)
                    .status(DocumentStatus.valueOf(status.toUpperCase()))
                    .classification(classification != null ? DocumentClassification.valueOf(classification.toUpperCase()) : null)
                    .uploadedAt(LocalDateTime.now())
                    .user(user)
                    .build();
            documentRepository.save(doc);
            log.debug("Created document: {}", doc.getId());
            return new DocumentCreateResponse(doc.getId(), doc.getName(), doc.getStatus().name());
        } catch (IllegalArgumentException e) {
            return validationError("status/classification", "Invalid enum value. Valid statuses: RECEIVED, PROCESSING, COMPLETED, FAILED. Valid classifications: BILL, ORDER, MENU");
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool createDocument error: {}", e.getMessage());
            return validationError("createDocument", e.getMessage());
        }
    }

    @Tool(description = "Update the status of an existing document. Provide documentId and the new status. Verifies the document belongs to the current user.")
    public Object updateDocumentStatus(
            @ToolParam(description = "The document ID to update") String documentId,
            @ToolParam(description = "New document status: RECEIVED, PROCESSING, OCR_PROCESSING, COMPLETED, or FAILED") String status
    ) {
        try {
            log.debug("MCP Tool updateDocumentStatus called with documentId: {}, status: {}", documentId, status);
            User user = getCurrentUser();
            Document doc = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
            if (!doc.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Document", documentId);
            }
            doc.setStatus(DocumentStatus.valueOf(status.toUpperCase()));
            documentRepository.save(doc);
            log.debug("Updated document {} status to {}", documentId, status);
            return new DocumentUpdateStatusResponse(doc.getId(), doc.getName(), doc.getStatus().name());
        } catch (IllegalArgumentException e) {
            return validationError("status", "Invalid status. Valid: RECEIVED, PROCESSING, OCR_PROCESSING, COMPLETED, FAILED", status);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Document", documentId);
        } catch (Exception e) {
            log.error("MCP Tool updateDocumentStatus error: {}", e.getMessage());
            return validationError("updateDocumentStatus", e.getMessage());
        }
    }

    @Tool(description = "Delete a document by ID. Verifies the document belongs to the current user before deletion.")
    public Object deleteDocument(
            @ToolParam(description = "The document ID to delete") String documentId
    ) {
        try {
            log.debug("MCP Tool deleteDocument called with id: {}", documentId);
            User user = getCurrentUser();
            Document doc = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
            if (!doc.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Document", documentId);
            }
            documentRepository.delete(doc);
            log.debug("Deleted document: {}", documentId);
            return new DocumentDeleteResponse(true, documentId);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Document", documentId);
        } catch (Exception e) {
            log.error("MCP Tool deleteDocument error: {}", e.getMessage());
            return validationError("deleteDocument", e.getMessage());
        }
    }

    // --- Document Read Tool T-D9 ---

    @Tool(description = "List all documents for the current user. Returns the full document list with id, name, type, size, date, status, and classification.")
    public Object getUserDocuments() {
        try {
            log.debug("MCP Tool getUserDocuments called");
            User user = getCurrentUser();
            return documentRepository.findByUser(user).stream()
                    .map(this::mapToDocumentInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getUserDocuments error: {}", e.getMessage());
            return validationError("getUserDocuments", e.getMessage());
        }
    }

    // --- Vendor Tool DTOs ---

    public record VendorInfo(
            String id,
            String name,
            String category,
            String contactInfo
    ) {}

    // --- Vendor Read Tools (T-V1 through T-V3) ---

    @Tool(description = "List all vendors for the current user. Use this to answer 'who are my vendors' or 'list suppliers' questions.")
    public Object getVendors() {
        try {
            log.debug("MCP Tool getVendors called");
            User user = getCurrentUser();
            return vendorRepository.findByUser(user).stream()
                    .map(this::mapToVendorInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getVendors error: {}", e.getMessage());
            return validationError("getVendors", e.getMessage());
        }
    }

    @Tool(description = "Get a specific vendor by ID. Verifies the vendor belongs to the current user.")
    public Object getVendorById(
            @ToolParam(description = "The vendor ID to retrieve") String vendorId
    ) {
        try {
            log.debug("MCP Tool getVendorById called with id: {}", vendorId);
            User user = getCurrentUser();
            Vendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
            if (!vendor.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Vendor", vendorId);
            }
            return mapToVendorInfo(vendor);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Vendor", vendorId);
        } catch (Exception e) {
            log.error("MCP Tool getVendorById error: {}", e.getMessage());
            return validationError("getVendorById", e.getMessage());
        }
    }

    @Tool(description = "Get vendors filtered by category (e.g., 'produce', 'dairy', 'meat'). Use this to find vendors by type.")
    public Object getVendorsByCategory(
            @ToolParam(description = "Vendor category to filter by") String category
    ) {
        try {
            log.debug("MCP Tool getVendorsByCategory called with category: {}", category);
            User user = getCurrentUser();
            return vendorRepository.findByUserAndCategory(user, category).stream()
                    .map(this::mapToVendorInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getVendorsByCategory error: {}", e.getMessage());
            return validationError("getVendorsByCategory", e.getMessage());
        }
    }

    // --- Vendor Write Tools (T-V4 through T-V6) ---

    public record VendorCreateResponse(String id, String name, String category) {}

    public record VendorUpdateResponse(String id, String name, String category, String contactInfo) {}

    public record VendorDeleteResponse(boolean deleted, String vendorId) {}

    @Tool(description = "Add a new vendor for the current user. At minimum, provide a name. Optionally provide category and contactInfo.")
    public Object createVendor(
            @ToolParam(description = "Vendor name") String name,
            @ToolParam(description = "Vendor category (e.g., produce, dairy, meat)", required = false) String category,
            @ToolParam(description = "Contact information (phone, email, etc.)", required = false) String contactInfo
    ) {
        try {
            log.debug("MCP Tool createVendor called with name: {}", name);
            User user = getCurrentUser();
            Vendor vendor = Vendor.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .name(name)
                    .category(category)
                    .contactInfo(contactInfo)
                    .user(user)
                    .build();
            vendorRepository.save(vendor);
            log.debug("Created vendor: {}", vendor.getId());
            return new VendorCreateResponse(vendor.getId(), vendor.getName(), vendor.getCategory());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool createVendor error: {}", e.getMessage());
            return validationError("createVendor", e.getMessage());
        }
    }

    @Tool(description = "Update an existing vendor's details. Provide the vendorId and any fields to update (name, category, contactInfo). Only provided fields are updated.")
    public Object updateVendor(
            @ToolParam(description = "The vendor ID to update") String vendorId,
            @ToolParam(description = "Updated vendor name", required = false) String name,
            @ToolParam(description = "Updated category", required = false) String category,
            @ToolParam(description = "Updated contact info", required = false) String contactInfo
    ) {
        try {
            log.debug("MCP Tool updateVendor called with id: {}", vendorId);
            User user = getCurrentUser();
            Vendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
            if (!vendor.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Vendor", vendorId);
            }
            if (name != null) vendor.setName(name);
            if (category != null) vendor.setCategory(category);
            if (contactInfo != null) vendor.setContactInfo(contactInfo);
            vendorRepository.save(vendor);
            log.debug("Updated vendor: {}", vendor.getId());
            return new VendorUpdateResponse(vendor.getId(), vendor.getName(), vendor.getCategory(), vendor.getContactInfo());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Vendor", vendorId);
        } catch (Exception e) {
            log.error("MCP Tool updateVendor error: {}", e.getMessage());
            return validationError("updateVendor", e.getMessage());
        }
    }

    @Tool(description = "Delete a vendor by ID. Verifies the vendor belongs to the current user before deletion.")
    public Object deleteVendor(
            @ToolParam(description = "The vendor ID to delete") String vendorId
    ) {
        try {
            log.debug("MCP Tool deleteVendor called with id: {}", vendorId);
            User user = getCurrentUser();
            Vendor vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
            if (!vendor.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Vendor", vendorId);
            }
            vendorRepository.delete(vendor);
            log.debug("Deleted vendor: {}", vendorId);
            return new VendorDeleteResponse(true, vendorId);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Vendor", vendorId);
        } catch (Exception e) {
            log.error("MCP Tool deleteVendor error: {}", e.getMessage());
            return validationError("deleteVendor", e.getMessage());
        }
    }

    // --- Inventory Tool DTOs ---

    public record InventoryItemInfo(
            String id,
            String name,
            BigDecimal currentStock,
            String unit,
            BigDecimal unitPrice,
            BigDecimal minThreshold,
            String category
    ) {}

    // --- Inventory Read Tools (T-I1 through T-I4) ---

    @Tool(description = "List all inventory items for the current user. Use this to answer 'show me my inventory' or 'what items do I have' questions.")
    public Object getInventoryItems() {
        try {
            log.debug("MCP Tool getInventoryItems called");
            User user = getCurrentUser();
            return inventoryItemRepository.findByUser(user).stream()
                    .map(this::mapToInventoryItemInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getInventoryItems error: {}", e.getMessage());
            return validationError("getInventoryItems", e.getMessage());
        }
    }

    @Tool(description = "Get inventory items that are below their minimum stock threshold. Use this to identify items that need reordering.")
    public Object getLowStockItems() {
        try {
            log.debug("MCP Tool getLowStockItems called");
            User user = getCurrentUser();
            return inventoryItemRepository.findLowStockByUser(user).stream()
                    .map(this::mapToInventoryItemInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getLowStockItems error: {}", e.getMessage());
            return validationError("getLowStockItems", e.getMessage());
        }
    }

    @Tool(description = "Get a specific inventory item by ID. Verifies the item belongs to the current user.")
    public Object getInventoryItemById(
            @ToolParam(description = "The inventory item ID to retrieve") String itemId
    ) {
        try {
            log.debug("MCP Tool getInventoryItemById called with id: {}", itemId);
            User user = getCurrentUser();
            InventoryItem item = inventoryItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
            if (!item.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Inventory item", itemId);
            }
            return mapToInventoryItemInfo(item);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Inventory item", itemId);
        } catch (Exception e) {
            log.error("MCP Tool getInventoryItemById error: {}", e.getMessage());
            return validationError("getInventoryItemById", e.getMessage());
        }
    }

    @Tool(description = "Get inventory items filtered by category (e.g., 'produce', 'dairy', 'meat'). Use this to find items by type.")
    public Object getInventoryByCategory(
            @ToolParam(description = "Category to filter by") String category
    ) {
        try {
            log.debug("MCP Tool getInventoryByCategory called with category: {}", category);
            User user = getCurrentUser();
            return inventoryItemRepository.findByUserAndCategory(user, category).stream()
                    .map(this::mapToInventoryItemInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool getInventoryByCategory error: {}", e.getMessage());
            return validationError("getInventoryByCategory", e.getMessage());
        }
    }

    // --- Inventory Write Tools (T-I5 through T-I8) ---

    public record InventoryItemCreateResponse(String id, String name, BigDecimal currentStock) {}

    public record InventoryItemUpdateStockResponse(
            String id,
            String name,
            BigDecimal previousStock,
            BigDecimal currentStock
    ) {}

    public record StockTransactionResponse(
            String transactionId,
            String itemId,
            String itemName,
            BigDecimal quantityChange,
            String type,
            BigDecimal newStock,
            LocalDateTime timestamp
    ) {}

    public record InventoryItemDeleteResponse(boolean deleted, String itemId) {}

    @Tool(description = "Add a new inventory item for the current user. At minimum, provide name, currentStock, and unit. Optionally provide unitPrice, minThreshold, and category.")
    public Object createInventoryItem(
            @ToolParam(description = "Item name") String name,
            @ToolParam(description = "Initial stock quantity") double currentStock,
            @ToolParam(description = "Unit of measure (e.g., kg, lbs, units)") String unit,
            @ToolParam(description = "Price per unit", required = false) Double unitPrice,
            @ToolParam(description = "Minimum stock threshold for alerts", required = false) Double minThreshold,
            @ToolParam(description = "Item category", required = false) String category
    ) {
        try {
            log.debug("MCP Tool createInventoryItem called with name: {}", name);
            User user = getCurrentUser();
            InventoryItem item = InventoryItem.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .name(name)
                    .currentStock(BigDecimal.valueOf(currentStock))
                    .unit(unit)
                    .unitPrice(unitPrice != null ? BigDecimal.valueOf(unitPrice) : null)
                    .minThreshold(minThreshold != null ? BigDecimal.valueOf(minThreshold) : null)
                    .category(category)
                    .user(user)
                    .build();
            inventoryItemRepository.save(item);
            log.debug("Created inventory item: {}", item.getId());
            return new InventoryItemCreateResponse(item.getId(), item.getName(), item.getCurrentStock());
        } catch (ResourceNotFoundException e) {
            return notFoundError("User", null);
        } catch (Exception e) {
            log.error("MCP Tool createInventoryItem error: {}", e.getMessage());
            return validationError("createInventoryItem", e.getMessage());
        }
    }

    @Tool(description = "Update the stock level of an inventory item. Provide the itemId and the new stock quantity.")
    public Object updateStock(
            @ToolParam(description = "The inventory item ID") String itemId,
            @ToolParam(description = "New stock quantity") double newStock
    ) {
        try {
            log.debug("MCP Tool updateStock called with itemId: {}", itemId);
            User user = getCurrentUser();
            InventoryItem item = inventoryItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
            if (!item.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Inventory item", itemId);
            }
            BigDecimal previousStock = item.getCurrentStock();
            item.setCurrentStock(BigDecimal.valueOf(newStock));
            inventoryItemRepository.save(item);
            log.debug("Updated stock for item {}: {} -> {}", itemId, previousStock, newStock);
            return new InventoryItemUpdateStockResponse(item.getId(), item.getName(), previousStock, item.getCurrentStock());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Inventory item", itemId);
        } catch (Exception e) {
            log.error("MCP Tool updateStock error: {}", e.getMessage());
            return validationError("updateStock", e.getMessage());
        }
    }

    @Tool(description = "Record a stock transaction (purchase, sale, or adjustment). Provide itemId, quantityChange (positive for purchase, negative for sale), and type (PURCHASE, SALE, or ADJUSTMENT). Optionally link to a documentId.")
    public Object createStockTransaction(
            @ToolParam(description = "The inventory item ID") String itemId,
            @ToolParam(description = "Quantity change (positive for purchase, negative for sale)") double quantityChange,
            @ToolParam(description = "Transaction type: PURCHASE, SALE, or ADJUSTMENT") String type,
            @ToolParam(description = "Optional linked document ID", required = false) String documentId
    ) {
        try {
            log.debug("MCP Tool createStockTransaction called with itemId: {}, type: {}", itemId, type);
            User user = getCurrentUser();
            InventoryItem item = inventoryItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
            if (!item.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Inventory item", itemId);
            }
            StockTransactionType txType = StockTransactionType.valueOf(type.toUpperCase());

            StockTransaction tx = StockTransaction.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .item(item)
                    .quantityChange(BigDecimal.valueOf(quantityChange))
                    .type(txType)
                    .timestamp(LocalDateTime.now())
                    .build();

            if (documentId != null) {
                Document doc = documentRepository.findById(documentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
                if (!doc.getUser().getId().equals(user.getId())) {
                    return accessDeniedError("Document", documentId);
                }
                tx.setDocument(doc);
            }

            stockTransactionRepository.save(tx);

            BigDecimal newStock = item.getCurrentStock().add(BigDecimal.valueOf(quantityChange));
            item.setCurrentStock(newStock);
            inventoryItemRepository.save(item);

            log.debug("Created stock transaction: {} for item {}, new stock: {}", tx.getId(), itemId, newStock);
            return new StockTransactionResponse(
                    tx.getId(),
                    item.getId(),
                    item.getName(),
                    tx.getQuantityChange(),
                    txType.name(),
                    newStock,
                    tx.getTimestamp()
            );
        } catch (IllegalArgumentException e) {
            return validationError("type", "Invalid transaction type. Valid: PURCHASE, SALE, ADJUSTMENT", type);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Inventory item/Document", itemId);
        } catch (Exception e) {
            log.error("MCP Tool createStockTransaction error: {}", e.getMessage());
            return validationError("createStockTransaction", e.getMessage());
        }
    }

    @Tool(description = "Delete an inventory item by ID. Verifies the item belongs to the current user before deletion.")
    public Object deleteInventoryItem(
            @ToolParam(description = "The inventory item ID to delete") String itemId
    ) {
        try {
            log.debug("MCP Tool deleteInventoryItem called with id: {}", itemId);
            User user = getCurrentUser();
            InventoryItem item = inventoryItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
            if (!item.getUser().getId().equals(user.getId())) {
                return accessDeniedError("Inventory item", itemId);
            }
            inventoryItemRepository.delete(item);
            log.debug("Deleted inventory item: {}", itemId);
            return new InventoryItemDeleteResponse(true, itemId);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Inventory item", itemId);
        } catch (Exception e) {
            log.error("MCP Tool deleteInventoryItem error: {}", e.getMessage());
            return validationError("deleteInventoryItem", e.getMessage());
        }
    }

    // --- Order Tool DTOs ---

    public record OrderSummaryInfo(
            String id,
            LocalDateTime orderDate,
            BigDecimal totalAmount,
            String status,
            int itemCount
    ) {}

    public record OrderItemInfo(
            String id,
            String menuItemName,
            Integer quantity,
            BigDecimal price
    ) {}

    public record OrderDetailInfo(
            String id,
            LocalDateTime orderDate,
            BigDecimal totalAmount,
            String status,
            List<OrderItemInfo> items
    ) {}

    // --- Order Read Tools (T-O1 through T-O4) ---

    @Tool(description = "Get the 10 most recent orders for the current user's restaurant. Use this to answer 'recent orders' or 'last 10 orders' questions.")
    @Transactional(readOnly = true)
    public Object getRecentOrders() {
        try {
            log.debug("MCP Tool getRecentOrders called");
            Restaurant restaurant = getCurrentRestaurant();
            return orderRepository.findTop10ByRestaurantOrderByOrderDateDesc(restaurant).stream()
                    .map(this::mapToOrderSummaryInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getRecentOrders error: {}", e.getMessage());
            return validationError("getRecentOrders", e.getMessage());
        }
    }

    @Tool(description = "Get a specific order by ID with all its items. Returns the order details including line items with menu item names, quantities, and prices.")
    @Transactional(readOnly = true)
    public Object getOrderById(
            @ToolParam(description = "The order ID to retrieve") String orderId
    ) {
        try {
            log.debug("MCP Tool getOrderById called with id: {}", orderId);
            Restaurant restaurant = getCurrentRestaurant();
            Order order = orderRepository.findByIdWithItems(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            if (!order.getRestaurant().getId().equals(restaurant.getId())) {
                return accessDeniedError("Order", orderId);
            }
            return mapToOrderDetailInfo(order);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Order", orderId);
        } catch (Exception e) {
            log.error("MCP Tool getOrderById error: {}", e.getMessage());
            return validationError("getOrderById", e.getMessage());
        }
    }

    @Tool(description = "Get orders within a date range for the current user's restaurant. Use dates in YYYY-MM-DD format. For example, 'orders from this week' or 'January orders'.")
    @Transactional(readOnly = true)
    public Object getOrdersByDateRange(
            @ToolParam(description = "Start date (YYYY-MM-DD)") String startDate,
            @ToolParam(description = "End date (YYYY-MM-DD)") String endDate
    ) {
        try {
            log.debug("MCP Tool getOrdersByDateRange called with range: {} to {}", startDate, endDate);
            Restaurant restaurant = getCurrentRestaurant();
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
            return orderRepository.findByRestaurantAndOrderDateBetween(restaurant, start, end).stream()
                    .map(this::mapToOrderSummaryInfo)
                    .collect(Collectors.toList());
        } catch (DateTimeException e) {
            return validationError("startDate/endDate", "Invalid date format. Use YYYY-MM-DD", startDate, endDate);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getOrdersByDateRange error: {}", e.getMessage());
            return validationError("getOrdersByDateRange", e.getMessage());
        }
    }

    @Tool(description = "Get orders filtered by status for the current user's restaurant. Use this to find pending, completed, or other status orders.")
    @Transactional(readOnly = true)
    public Object getOrdersByStatus(
            @ToolParam(description = "Order status to filter by (e.g., PENDING, COMPLETED, PREPARING)") String status
    ) {
        try {
            log.debug("MCP Tool getOrdersByStatus called with status: {}", status);
            Restaurant restaurant = getCurrentRestaurant();
            return orderRepository.findByRestaurantAndStatus(restaurant, status.toUpperCase()).stream()
                    .map(this::mapToOrderSummaryInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getOrdersByStatus error: {}", e.getMessage());
            return validationError("getOrdersByStatus", e.getMessage());
        }
    }

    // --- Order Write Tools (T-O5 through T-O7) ---

    public record OrderCreateResponse(
            String id,
            LocalDateTime orderDate,
            BigDecimal totalAmount,
            String status,
            int itemCount
    ) {}

    public record OrderStatusUpdateResponse(
            String id,
            String status,
            LocalDateTime orderDate
    ) {}

    public record OrderItemAddResponse(
            String orderId,
            String orderItemId,
            BigDecimal newTotalAmount
    ) {}

    @Tool(description = "Create a new order for the current user's restaurant. Provide totalAmount and optionally a status (default PENDING) and items with menuItemId, quantity, and price. Use getMenuItems first to get valid menuItemIds.")
    public Object createOrder(
            @ToolParam(description = "Total order amount") double totalAmount,
            @ToolParam(description = "Initial order status (default: PENDING)", required = false) String status,
            @ToolParam(description = "Order items array with menuItemId, quantity, and price", required = false) List<OrderItemInput> items
    ) {
        try {
            log.debug("MCP Tool createOrder called with totalAmount: {}", totalAmount);
            Restaurant restaurant = getCurrentRestaurant();

            Order order = Order.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .orderDate(LocalDateTime.now())
                    .totalAmount(BigDecimal.valueOf(totalAmount))
                    .status(status != null ? status.toUpperCase() : "PENDING")
                    .restaurant(restaurant)
                    .items(new java.util.ArrayList<>())
                    .build();

            if (items != null && !items.isEmpty()) {
                for (OrderItemInput itemInput : items) {
                    MenuItem menuItem = menuItemRepository.findById(itemInput.menuItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemInput.menuItemId()));

                    OrderItem orderItem = OrderItem.builder()
                            .id(java.util.UUID.randomUUID().toString())
                            .order(order)
                            .menuItem(menuItem)
                            .quantity(itemInput.quantity())
                            .price(menuItem.getPrice())
                            .build();
                    order.getItems().add(orderItem);
                }
                BigDecimal computedTotal = order.getItems().stream()
                        .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                order.setTotalAmount(computedTotal);
            }

            orderRepository.save(order);
            log.debug("Created order: {} with {} items", order.getId(), order.getItems().size());
            return new OrderCreateResponse(
                    order.getId(),
                    order.getOrderDate(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getItems().size()
            );
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant/Menu item", null);
        } catch (Exception e) {
            log.error("MCP Tool createOrder error: {}", e.getMessage());
            return validationError("createOrder", e.getMessage());
        }
    }

    @Tool(description = "Update the status of an existing order. Provide orderId and the new status (e.g., PENDING, PREPARING, COMPLETED, CANCELLED). Verifies the order belongs to the current user's restaurant.")
    public Object updateOrderStatus(
            @ToolParam(description = "The order ID to update") String orderId,
            @ToolParam(description = "New order status (e.g., PENDING, PREPARING, COMPLETED, CANCELLED)") String status
    ) {
        try {
            log.debug("MCP Tool updateOrderStatus called with orderId: {}, status: {}", orderId, status);
            Restaurant restaurant = getCurrentRestaurant();
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            if (!order.getRestaurant().getId().equals(restaurant.getId())) {
                return accessDeniedError("Order", orderId);
            }
            order.setStatus(status.toUpperCase());
            orderRepository.save(order);
            log.debug("Updated order {} status to {}", orderId, status);
            return new OrderStatusUpdateResponse(order.getId(), order.getStatus(), order.getOrderDate());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Order", orderId);
        } catch (Exception e) {
            log.error("MCP Tool updateOrderStatus error: {}", e.getMessage());
            return validationError("updateOrderStatus", e.getMessage());
        }
    }

    @Tool(description = "Add an item to an existing order. Provide orderId, menuItemId, quantity, and price. The price will be snapshotted from the menu item. Verifies the order belongs to the current user's restaurant.")
    public Object addOrderItem(
            @ToolParam(description = "The order ID to add an item to") String orderId,
            @ToolParam(description = "Menu item ID to add") String menuItemId,
            @ToolParam(description = "Quantity ordered") int quantity,
            @ToolParam(description = "Price per unit") double price
    ) {
        try {
            log.debug("MCP Tool addOrderItem called with orderId: {}, menuItemId: {}", orderId, menuItemId);
            Restaurant restaurant = getCurrentRestaurant();
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            if (!order.getRestaurant().getId().equals(restaurant.getId())) {
                return accessDeniedError("Order", orderId);
            }

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + menuItemId));

            OrderItem orderItem = OrderItem.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(quantity)
                    .price(BigDecimal.valueOf(price))
                    .build();
            order.getItems().add(orderItem);

            BigDecimal newTotal = order.getItems().stream()
                    .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(newTotal);

            orderRepository.save(order);
            log.debug("Added item {} to order {}, new total: {}", orderItem.getId(), orderId, newTotal);
            return new OrderItemAddResponse(order.getId(), orderItem.getId(), order.getTotalAmount());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Order/Menu item", orderId);
        } catch (Exception e) {
            log.error("MCP Tool addOrderItem error: {}", e.getMessage());
            return validationError("addOrderItem", e.getMessage());
        }
    }

    public record OrderItemInput(String menuItemId, int quantity, double price) {}

    // --- Menu Tool DTOs ---

    public record MenuItemInfo(
            String id,
            String name,
            String description,
            BigDecimal price,
            String category
    ) {}

    // --- Menu Read Tools (T-M1 through T-M2) ---

    @Tool(description = "List all menu items for the current user's restaurant. Use this to answer 'what's on the menu' or 'show me all menu items' questions.")
    public Object getMenuItems() {
        try {
            log.debug("MCP Tool getMenuItems called");
            Restaurant restaurant = getCurrentRestaurant();
            return menuItemRepository.findByRestaurant(restaurant).stream()
                    .map(this::mapToMenuItemInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getMenuItems error: {}", e.getMessage());
            return validationError("getMenuItems", e.getMessage());
        }
    }

    @Tool(description = "Get menu items filtered by category (e.g., 'appetizers', 'desserts', 'entrees'). Use this to find menu items by type.")
    public Object getMenuItemsByCategory(
            @ToolParam(description = "Menu category to filter by") String category
    ) {
        try {
            log.debug("MCP Tool getMenuItemsByCategory called with category: {}", category);
            Restaurant restaurant = getCurrentRestaurant();
            return menuItemRepository.findByRestaurantAndCategory(restaurant, category).stream()
                    .map(this::mapToMenuItemInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getMenuItemsByCategory error: {}", e.getMessage());
            return validationError("getMenuItemsByCategory", e.getMessage());
        }
    }

    // --- Metrics Tool DTOs ---

    public record RestaurantMetricInfo(
            String id,
            String metricName,
            BigDecimal metricValue,
            LocalDateTime periodStart,
            LocalDateTime periodEnd
    ) {}

    // --- Metrics Read Tools (T-R1) ---

    @Tool(description = "Get performance metrics for the current user's restaurant. Returns metric name, value, and the period it covers. Use this to answer 'what are my metrics' or 'show me performance data' questions.")
    public Object getRestaurantMetrics() {
        try {
            log.debug("MCP Tool getRestaurantMetrics called");
            Restaurant restaurant = getCurrentRestaurant();
            return restaurantMetricRepository.findByRestaurant(restaurant).stream()
                    .map(this::mapToRestaurantMetricInfo)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getRestaurantMetrics error: {}", e.getMessage());
            return validationError("getRestaurantMetrics", e.getMessage());
        }
    }

    // --- Subscription Tool DTOs ---

    public record SubscriptionInfo(
            String planName,
            BigDecimal planPrice,
            Long maxDocumentsSizeMb,
            Integer maxChatsPerMonth,
            Integer maxAccountsPerRestaurant,
            Double currentDocumentsSizeMb,
            Integer currentChatsCount,
            Integer currentNotificationsCount,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {}

    public record SubscriptionLimitCheckResponse(
            boolean allowed,
            String actionType,
            int currentUsage,
            int limit,
            int remaining
    ) {}

    public record SubscriptionUsageIncrementResponse(
            String actionType,
            int newCount,
            String monthYear
    ) {}

    public record SubscriptionPlanInfo(
            String id,
            String name,
            BigDecimal price,
            Long maxDocumentsSizeMb,
            Integer maxChatsPerMonth,
            Integer maxAccountsPerRestaurant,
            Integer maxNotificationsPerMonth
    ) {}

    public record AccountCountResponse(long count) {}

    // --- Subscription Read Tools (T-S1) ---

    @Tool(description = "Get current subscription plan and usage for the restaurant. Returns plan name, price, limits, current usage, and subscription dates. Use this to answer 'what's my subscription' or 'show me my plan limits' questions.")
    public Object getSubscriptionInfo() {
        try {
            log.debug("MCP Tool getSubscriptionInfo called");
            Restaurant restaurant = getCurrentRestaurant();

            var subscription = restaurantSubscriptionRepository.findByRestaurant(restaurant).orElse(null);
            String monthYear = java.time.YearMonth.now().toString();
            var usage = subscriptionUsageRepository.findByRestaurantAndMonthYear(restaurant, monthYear).orElse(null);

            String planName = null;
            BigDecimal planPrice = null;
            Long maxDocumentsSizeMb = null;
            Integer maxChatsPerMonth = null;
            Integer maxAccountsPerRestaurant = null;
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (subscription != null && subscription.getPlan() != null) {
                var plan = subscription.getPlan();
                planName = plan.getName();
                planPrice = plan.getPrice();
                maxDocumentsSizeMb = plan.getMaxDocumentsSizeMb();
                maxChatsPerMonth = plan.getMaxChatsPerMonth();
                maxAccountsPerRestaurant = plan.getMaxAccountsPerRestaurant();
                startDate = subscription.getStartDate();
                endDate = subscription.getEndDate();
            }

            Double currentDocumentsSizeMb = null;
            Integer currentChatsCount = null;
            Integer currentNotificationsCount = null;

            if (usage != null) {
                currentDocumentsSizeMb = usage.getCurrentDocumentsSizeBytes() != null
                        ? usage.getCurrentDocumentsSizeBytes() / (1024.0 * 1024.0) : null;
                currentChatsCount = usage.getChatsCount();
                currentNotificationsCount = usage.getNotificationsCount();
            }

            return new SubscriptionInfo(
                    planName, planPrice, maxDocumentsSizeMb, maxChatsPerMonth, maxAccountsPerRestaurant,
                    currentDocumentsSizeMb, currentChatsCount, currentNotificationsCount,
                    startDate, endDate
            );
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getSubscriptionInfo error: {}", e.getMessage());
            return validationError("getSubscriptionInfo", e.getMessage());
        }
    }

    // --- Subscription Write Tools (T-S2 through T-S3) ---

    @Tool(description = "Check if an action is within subscription tier limits. Provide actionType: CHAT, DOCUMENT_UPLOAD, or NOTIFICATION. Returns whether the action is allowed, current usage, limit, and remaining capacity.")
    public Object checkSubscriptionLimit(
            @ToolParam(description = "Type of action to check: CHAT, DOCUMENT_UPLOAD, or NOTIFICATION") String actionType
    ) {
        try {
            log.debug("MCP Tool checkSubscriptionLimit called with actionType: {}", actionType);
            Restaurant restaurant = getCurrentRestaurant();

            var subscription = restaurantSubscriptionRepository.findByRestaurant(restaurant).orElse(null);
            String monthYear = YearMonth.now().toString();
            var usage = subscriptionUsageRepository.findByRestaurantAndMonthYear(restaurant, monthYear).orElse(null);

            if (subscription == null || subscription.getPlan() == null) {
                return new SubscriptionLimitCheckResponse(false, actionType, 0, 0, 0);
            }

            var plan = subscription.getPlan();
            int currentUsage = 0;
            int limit = 0;

            switch (actionType.toUpperCase()) {
                case "CHAT":
                    currentUsage = usage != null && usage.getChatsCount() != null ? usage.getChatsCount() : 0;
                    limit = plan.getMaxChatsPerMonth() != null ? plan.getMaxChatsPerMonth() : 0;
                    break;
                case "DOCUMENT_UPLOAD":
                    long currentBytes = usage != null && usage.getCurrentDocumentsSizeBytes() != null ? usage.getCurrentDocumentsSizeBytes() : 0;
                    long maxBytes = plan.getMaxDocumentsSizeMb() != null ? plan.getMaxDocumentsSizeMb() * 1024 * 1024 : 0;
                    currentUsage = (int) (currentBytes / (1024 * 1024));
                    limit = (int) (maxBytes / (1024 * 1024));
                    break;
                case "NOTIFICATION":
                    currentUsage = usage != null && usage.getNotificationsCount() != null ? usage.getNotificationsCount() : 0;
                    limit = plan.getMaxNotificationsPerMonth() != null ? plan.getMaxNotificationsPerMonth() : 0;
                    break;
                case "ACCOUNTS":
                    currentUsage = (int) userRepository.countByRestaurant(restaurant);
                    limit = plan.getMaxAccountsPerRestaurant() != null ? plan.getMaxAccountsPerRestaurant() : 0;
                    break;
                default:
                    return validationError("actionType", "Invalid action type. Valid: CHAT, DOCUMENT_UPLOAD, NOTIFICATION", actionType);
            }

            boolean allowed = currentUsage < limit;
            int remaining = Math.max(0, limit - currentUsage);
            log.debug("Subscription limit check for {}: allowed={}, usage={}, limit={}", actionType, allowed, currentUsage, limit);
            return new SubscriptionLimitCheckResponse(allowed, actionType, currentUsage, limit, remaining);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool checkSubscriptionLimit error: {}", e.getMessage());
            return validationError("checkSubscriptionLimit", e.getMessage());
        }
    }

    @Transactional
    @Tool(description = "Increment subscription usage counter. Provide actionType: CHAT, DOCUMENT_UPLOAD, or NOTIFICATION. For DOCUMENT_UPLOAD, provide the size in bytes. Returns the new count and the month year.")
    public Object incrementUsage(
            @ToolParam(description = "Type of usage to increment: CHAT, DOCUMENT_UPLOAD, or NOTIFICATION") String actionType,
            @ToolParam(description = "Amount to increment (default: 1). For DOCUMENT_UPLOAD, provide size in bytes.", required = false) Integer amount
    ) {
        try {
            log.debug("MCP Tool incrementUsage called with actionType: {}, amount: {}", actionType, amount);
            Restaurant restaurant = getCurrentRestaurant();
            String monthYear = YearMonth.now().toString();

            var usage = subscriptionUsageRepository.findByRestaurantAndMonthYear(restaurant, monthYear)
                    .orElseGet(() -> {
                        SubscriptionUsage newUsage = SubscriptionUsage.builder()
                                .id(java.util.UUID.randomUUID().toString())
                                .restaurant(restaurant)
                                .monthYear(monthYear)
                                .currentDocumentsSizeBytes(0L)
                                .chatsCount(0)
                                .notificationsCount(0)
                                .build();
                        return subscriptionUsageRepository.save(newUsage);
                    });

            int increment = amount != null ? amount : 1;
            int newCount = 0;

            switch (actionType.toUpperCase()) {
                case "CHAT":
                    usage.setChatsCount((usage.getChatsCount() != null ? usage.getChatsCount() : 0) + increment);
                    newCount = usage.getChatsCount();
                    break;
                case "DOCUMENT_UPLOAD":
                    usage.setCurrentDocumentsSizeBytes(
                            (usage.getCurrentDocumentsSizeBytes() != null ? usage.getCurrentDocumentsSizeBytes() : 0L) + increment
                    );
                    newCount = (int) (usage.getCurrentDocumentsSizeBytes() / (1024 * 1024));
                    break;
                case "NOTIFICATION":
                    usage.setNotificationsCount((usage.getNotificationsCount() != null ? usage.getNotificationsCount() : 0) + increment);
                    newCount = usage.getNotificationsCount();
                    break;
                default:
                    return validationError("actionType", "Invalid action type. Valid: CHAT, DOCUMENT_UPLOAD, NOTIFICATION", actionType);
            }

            subscriptionUsageRepository.save(usage);
            log.debug("Incremented {} usage by {}, new count: {}", actionType, increment, newCount);
            return new SubscriptionUsageIncrementResponse(actionType, newCount, monthYear);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool incrementUsage error: {}", e.getMessage());
            return validationError("incrementUsage", e.getMessage());
        }
    }

    // --- Subscription Read Tools (T-S4, T-S5) ---

    @Tool(description = "List all available subscription plans. Returns plan details including name, price, and limits.")
    public Object getAllPlans() {
        try {
            log.debug("MCP Tool getAllPlans called");
            return subscriptionPlanRepository.findAll().stream()
                    .map(plan -> new SubscriptionPlanInfo(
                            plan.getId(),
                            plan.getName(),
                            plan.getPrice(),
                            plan.getMaxDocumentsSizeMb(),
                            plan.getMaxChatsPerMonth(),
                            plan.getMaxAccountsPerRestaurant(),
                            plan.getMaxNotificationsPerMonth()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("MCP Tool getAllPlans error: {}", e.getMessage());
            return validationError("getAllPlans", e.getMessage());
        }
    }

    @Tool(description = "Count the number of user accounts associated with the current user's restaurant. Useful for checking account limits.")
    public Object getAccountCount() {
        try {
            log.debug("MCP Tool getAccountCount called");
            Restaurant restaurant = getCurrentRestaurant();
            long count = userRepository.countByRestaurant(restaurant);
            return new AccountCountResponse(count);
        } catch (ResourceNotFoundException e) {
            return notFoundError("Restaurant", null);
        } catch (Exception e) {
            log.error("MCP Tool getAccountCount error: {}", e.getMessage());
            return validationError("getAccountCount", e.getMessage());
        }
    }

    // --- Mappers ---

    private DocumentInfo mapToDocumentInfo(Document doc) {
        return new DocumentInfo(
                doc.getId(),
                doc.getName(),
                doc.getType(),
                doc.getSize(),
                doc.getDate(),
                doc.getUploadedAt(),
                doc.getStatus() != null ? doc.getStatus().name() : null,
                doc.getClassification() != null ? doc.getClassification().name() : null
        );
    }

    private VendorInfo mapToVendorInfo(Vendor vendor) {
        return new VendorInfo(
                vendor.getId(),
                vendor.getName(),
                vendor.getCategory(),
                vendor.getContactInfo()
        );
    }

    private InventoryItemInfo mapToInventoryItemInfo(InventoryItem item) {
        return new InventoryItemInfo(
                item.getId(),
                item.getName(),
                item.getCurrentStock(),
                item.getUnit(),
                item.getUnitPrice(),
                item.getMinThreshold(),
                item.getCategory()
        );
    }

    private OrderSummaryInfo mapToOrderSummaryInfo(Order order) {
        int itemCount = 0;
        try {
            if (order.getItems() != null) {
                itemCount = order.getItems().size();
            }
        } catch (Exception e) {
            log.warn("Could not load items for order {}: {}", order.getId(), e.getMessage());
        }
        return new OrderSummaryInfo(
                order.getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                itemCount
        );
    }

    private OrderDetailInfo mapToOrderDetailInfo(Order order) {
        List<OrderItemInfo> items = List.of();
        try {
            if (order.getItems() != null) {
                items = order.getItems().stream()
                        .map(oi -> new OrderItemInfo(
                                oi.getId(),
                                oi.getMenuItem() != null ? oi.getMenuItem().getName() : null,
                                oi.getQuantity(),
                                oi.getPrice()
                        ))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Could not load order items for order {}: {}", order.getId(), e.getMessage());
        }
        return new OrderDetailInfo(
                order.getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                items
        );
    }

    private MenuItemInfo mapToMenuItemInfo(MenuItem menuItem) {
        return new MenuItemInfo(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory()
        );
    }

    private RestaurantMetricInfo mapToRestaurantMetricInfo(RestaurantMetric metric) {
        return new RestaurantMetricInfo(
                metric.getId(),
                metric.getMetricName(),
                metric.getMetricValue(),
                metric.getPeriodStart(),
                metric.getPeriodEnd()
        );
    }
}
