package com.example.SuChefService.chat;

public enum QueryIntent {
    // Restaurant
    RESTAURANT_NAME,

    // Documents
    DOCUMENT_COUNT,
    DOCUMENT_LIST,
    DOCUMENT_SEARCH,
    DOCUMENT_LATEST,
    DOCUMENT_BY_STATUS,

    // Orders
    ORDER_RECENT,
    ORDER_BY_ID,
    ORDER_BY_STATUS,
    ORDER_COUNT,

    // Inventory
    INVENTORY_LIST,
    INVENTORY_LOW_STOCK,
    INVENTORY_BY_CATEGORY,

    // Vendors
    VENDOR_LIST,
    VENDOR_BY_CATEGORY,

    // Menu
    MENU_LIST,
    MENU_BY_CATEGORY,

    // Metrics
    RESTAURANT_METRICS,

    // Subscription
    SUBSCRIPTION_INFO,

    // Fallback
    GENERAL_QUESTION
}
