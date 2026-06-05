package com.example.SuChefService.chat;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class IntentClassifier {

    public ClassifiedIntent classify(String message) {
        String lower = message.toLowerCase().trim();

        // Restaurant
        if (matches(lower, "restaurant name", "what.*called", "name of my restaurant", "my restaurant name"))
            return new ClassifiedIntent(QueryIntent.RESTAURANT_NAME);

        // Documents
        if (matches(lower, "how many documents", "document count", "total documents", "number of documents"))
            return new ClassifiedIntent(QueryIntent.DOCUMENT_COUNT);
        if (matches(lower, "show.*documents", "list.*documents", "my documents", "all documents", "what documents"))
            return new ClassifiedIntent(QueryIntent.DOCUMENT_LIST);
        if (matches(lower, "search.*document", "find.*document"))
            return extractSearchQuery(lower, QueryIntent.DOCUMENT_SEARCH);
        if (matches(lower, "latest document", "last.*uploaded", "most recent document"))
            return new ClassifiedIntent(QueryIntent.DOCUMENT_LATEST);
        if (matches(lower, "failed documents", "processing documents", "completed documents"))
            return extractStatus(lower, QueryIntent.DOCUMENT_BY_STATUS);

        // Orders
        if (matches(lower, "recent orders", "last.*orders", "show.*orders", "my orders", "latest orders", "what orders"))
            return new ClassifiedIntent(QueryIntent.ORDER_RECENT);
        if (matches(lower, "order count", "how many orders", "total orders", "number of orders"))
            return new ClassifiedIntent(QueryIntent.ORDER_COUNT);
        if (matches(lower, "pending orders", "completed orders", "preparing orders", "cancelled orders"))
            return extractStatus(lower, QueryIntent.ORDER_BY_STATUS);

        // Inventory
        if (matches(lower, "low stock", "running low", "need reorder", "stock alert", "out of stock"))
            return new ClassifiedIntent(QueryIntent.INVENTORY_LOW_STOCK);
        if (matches(lower, "inventory", "stock", "items.*have", "what.*stock"))
            return new ClassifiedIntent(QueryIntent.INVENTORY_LIST);

        // Vendors
        if (matches(lower, "vendor", "supplier"))
            return new ClassifiedIntent(QueryIntent.VENDOR_LIST);

        // Menu
        if (matches(lower, "menu", "dishes", "food items", "what.*serve"))
            return new ClassifiedIntent(QueryIntent.MENU_LIST);

        // Metrics
        if (matches(lower, "metrics", "performance", "analytics", "revenue", "profit"))
            return new ClassifiedIntent(QueryIntent.RESTAURANT_METRICS);

        // Subscription
        if (matches(lower, "subscription", "my plan", "plan limits", "current plan"))
            return new ClassifiedIntent(QueryIntent.SUBSCRIPTION_INFO);

        return new ClassifiedIntent(QueryIntent.GENERAL_QUESTION);
    }

    private boolean matches(String text, String... patterns) {
        for (String p : patterns) {
            if (Pattern.compile(".*" + p + ".*").matcher(text).matches()) return true;
        }
        return false;
    }

    private ClassifiedIntent extractSearchQuery(String lower, QueryIntent intent) {
        return new ClassifiedIntent(intent);
    }

    private ClassifiedIntent extractStatus(String lower, QueryIntent intent) {
        if (lower.contains("pending")) return new ClassifiedIntent(intent, "PENDING");
        if (lower.contains("completed")) return new ClassifiedIntent(intent, "COMPLETED");
        if (lower.contains("processing") || lower.contains("preparing")) return new ClassifiedIntent(intent, "PREPARING");
        if (lower.contains("cancelled")) return new ClassifiedIntent(intent, "CANCELLED");
        if (lower.contains("failed")) return new ClassifiedIntent(intent, "FAILED");
        if (lower.contains("received")) return new ClassifiedIntent(intent, "RECEIVED");
        return new ClassifiedIntent(intent);
    }

    public record ClassifiedIntent(QueryIntent intent, String filter) {
        public ClassifiedIntent(QueryIntent intent) {
            this(intent, null);
        }
    }
}
