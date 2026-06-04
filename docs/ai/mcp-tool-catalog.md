# MCP Tool Catalog

**Epic:** EPIC-MCP-001 — Natural Language Data Access via MCP Server  
**Task:** T-1.3 — Design MCP Tool Catalog  
**Status:** Done  
**Date:** 2026-05-30  
**Author:** orchestrator + architect

---

## 1. Overview

This document defines the complete set of MCP tools covering ALL database operations for the SuChef application. Every service's DB interaction maps to exactly one tool. Tools are organized by domain entity and分为 read vs write categories.

**Total tools: 38** (21 read + 17 write)

### Tool Count by Domain

| Domain | Read Tools | Write Tools | Total |
|--------|-----------|-------------|-------|
| Document | 6 | 3 | 9 |
| Vendor | 3 | 3 | 6 |
| Inventory | 4 | 4 | 8 |
| Order | 4 | 3 | 7 |
| Menu | 2 | 0 | 2 |
| Metrics | 1 | 0 | 1 |
| Subscription | 3 | 2 | 5 |
| **Total** | **21** | **17** | **38** |

---

## 2. Multi-Tenant Isolation Model

All tools enforce tenant boundaries via `SecurityContextHolder`. The isolation hierarchy:

```
JWT Token
  └─ email → User (via userRepository.findByEmail)
       └─ user.restaurant → Restaurant (via ManyToOne)
            └─ All queries scoped to this Restaurant or User
```

### Scoping Levels

| Level | Scope Key | Applies To |
|-------|-----------|------------|
| **User-scoped** | `user` (resolved from JWT) | Documents, Vendors, InventoryItems |
| **Restaurant-scoped** | `user.restaurant` (resolved from JWT) | Orders, MenuItems, Metrics, Subscriptions, OrderItems |

Every tool description includes the scoping rule. The `McpToolProvider` class will resolve the current user and restaurant once per tool call, then pass the scoped entity to repository methods.

---

## 3. Tool Catalog — Documents

### T-D1: `getDocumentCount`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get the total number of documents uploaded by the current user |
| **Scope** | User-scoped |
| **Replaces** | `DocumentService.getDocumentCount()` |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "count": { "type": "integer", "description": "Total document count" }
  }
}
```

**Example NL Prompts:** "How many documents do I have?", "What's my document count?"

---

### T-D2: `getDocumentById`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get details of a specific document by its ID |
| **Scope** | User-scoped (ownership verified) |
| **Replaces** | `DocumentService.getDocumentById()` |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "documentId": { "type": "string", "description": "The document ID" }
  },
  "required": ["documentId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "name": { "type": "string" },
    "type": { "type": "string" },
    "size": { "type": "integer" },
    "date": { "type": "string", "format": "date-time" },
    "uploadedAt": { "type": "string", "format": "date-time" },
    "status": { "type": "string", "enum": ["RECEIVED", "PROCESSING", "OCR_PROCESSING", "COMPLETED", "FAILED"] },
    "classification": { "type": "string", "enum": ["BILL", "ORDER", "MENU", "null"] }
  }
}
```

**Example NL Prompts:** "Show me document abc123", "Get details for my receipt"

---

### T-D3: `getDocumentsByStatus`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get all documents filtered by status |
| **Scope** | User-scoped |
| **Replaces** | `DocumentService.getDocumentsByStatus()` |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "status": {
      "type": "string",
      "enum": ["RECEIVED", "PROCESSING", "OCR_PROCESSING", "COMPLETED", "FAILED"],
      "description": "Document status to filter by"
    }
  },
  "required": ["status"]
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "type": { "type": "string" },
      "uploadedAt": { "type": "string", "format": "date-time" },
      "status": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Show me completed documents", "Which documents are still processing?"

---

### T-D4: `getLastUploadedDocument`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get the most recently uploaded document |
| **Scope** | User-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "name": { "type": "string" },
    "type": { "type": "string" },
    "uploadedAt": { "type": "string", "format": "date-time" },
    "status": { "type": "string" }
  },
  "nullable": true
}
```

**Example NL Prompts:** "What was my last uploaded document?", "Show me the latest document"

---

### T-D5: `searchDocuments`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Search documents by name substring |
| **Scope** | User-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "nameQuery": { "type": "string", "description": "Name substring to search for (case-insensitive)" }
  },
  "required": ["nameQuery"]
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "type": { "type": "string" },
      "uploadedAt": { "type": "string", "format": "date-time" },
      "status": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Find documents named receipt", "Search for invoices"

---

### T-D6: `createDocument`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Store a new document record in the database |
| **Scope** | User-scoped (document assigned to current user) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "name": { "type": "string", "description": "Document file name" },
    "type": { "type": "string", "description": "MIME type (e.g., application/pdf)" },
    "size": { "type": "integer", "description": "File size in bytes" },
    "status": {
      "type": "string",
      "enum": ["RECEIVED", "PROCESSING", "COMPLETED", "FAILED"],
      "description": "Initial document status"
    },
    "classification": {
      "type": "string",
      "enum": ["BILL", "ORDER", "MENU"],
      "description": "Document classification"
    }
  },
  "required": ["name", "type", "status"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string", "description": "Generated document ID" },
    "name": { "type": "string" },
    "status": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Save document receipt.pdf", "Create a new document record for my invoice"

---

### T-D7: `updateDocumentStatus`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Update the status of an existing document |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "documentId": { "type": "string", "description": "The document ID" },
    "status": {
      "type": "string",
      "enum": ["RECEIVED", "PROCESSING", "OCR_PROCESSING", "COMPLETED", "FAILED"],
      "description": "New document status"
    }
  },
  "required": ["documentId", "status"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "name": { "type": "string" },
    "status": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Mark document abc123 as completed", "Update status to processing"

---

### T-D8: `deleteDocument`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Delete a document by ID |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "documentId": { "type": "string", "description": "The document ID to delete" }
  },
  "required": ["documentId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "deleted": { "type": "boolean" },
    "documentId": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Delete document abc123", "Remove my receipt"

---

### T-D9: `getUserDocuments`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get all documents for the current user (paginated list) |
| **Scope** | User-scoped |
| **Added** | T-4.10 (Phase 4) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "type": { "type": "string" },
      "size": { "type": "integer" },
      "uploadedAt": { "type": "string", "format": "date-time" },
      "status": { "type": "string" },
      "classification": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Show me all my documents", "List my uploaded files"

---

## 4. Tool Catalog — Vendors

### T-V1: `getVendors`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | List all vendors for the current user |
| **Scope** | User-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "category": { "type": "string" },
      "contactInfo": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Who are my vendors?", "List all my suppliers"

---

### T-V2: `getVendorById`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get a specific vendor by ID |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "vendorId": { "type": "string", "description": "The vendor ID" }
  },
  "required": ["vendorId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "name": { "type": "string" },
    "category": { "type": "string" },
    "contactInfo": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Show me vendor abc123", "Get details for Fresh Produce Co"

---

### T-V3: `getVendorsByCategory`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get vendors filtered by category |
| **Scope** | User-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "category": { "type": "string", "description": "Vendor category to filter by" }
  },
  "required": ["category"]
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "category": { "type": "string" },
      "contactInfo": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Show me my produce vendors", "Which vendors supply dairy?"

---

### T-V4: `createVendor`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Add a new vendor |
| **Scope** | User-scoped (vendor assigned to current user) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "name": { "type": "string", "description": "Vendor name" },
    "category": { "type": "string", "description": "Vendor category (e.g., produce, dairy, meat)" },
    "contactInfo": { "type": "string", "description": "Contact information (phone, email, etc.)" }
  },
  "required": ["name"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string", "description": "Generated vendor ID" },
    "name": { "type": "string" },
    "category": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Add a new vendor called Fresh Farms", "Create vendor Fresh Farms in produce category"

---

### T-V5: `updateVendor`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Update an existing vendor's details |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "vendorId": { "type": "string", "description": "The vendor ID" },
    "name": { "type": "string", "description": "Updated vendor name" },
    "category": { "type": "string", "description": "Updated category" },
    "contactInfo": { "type": "string", "description": "Updated contact info" }
  },
  "required": ["vendorId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "name": { "type": "string" },
    "category": { "type": "string" },
    "contactInfo": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Update vendor abc123 phone number to 555-0123", "Change Fresh Farms category to organic"

---

### T-V6: `deleteVendor`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Remove a vendor |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "vendorId": { "type": "string", "description": "The vendor ID to delete" }
  },
  "required": ["vendorId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "deleted": { "type": "boolean" },
    "vendorId": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Delete vendor abc123", "Remove Fresh Farms from my vendors"

---

## 5. Tool Catalog — Inventory

### T-I1: `getInventoryItems`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | List all inventory items for the current user |
| **Scope** | User-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "currentStock": { "type": "number" },
      "unit": { "type": "string" },
      "unitPrice": { "type": "number" },
      "minThreshold": { "type": "number" },
      "category": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Show me my inventory", "What items do I have in stock?"

---

### T-I2: `getLowStockItems`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get inventory items below their minimum threshold |
| **Scope** | User-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "currentStock": { "type": "number" },
      "unit": { "type": "string" },
      "unitPrice": { "type": "number" },
      "minThreshold": { "type": "number" },
      "category": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "What items are low on stock?", "Which products need reordering?"

---

### T-I3: `getInventoryItemById`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get a specific inventory item by ID |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "itemId": { "type": "string", "description": "The inventory item ID" }
  },
  "required": ["itemId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "name": { "type": "string" },
    "currentStock": { "type": "number" },
    "unit": { "type": "string" },
    "unitPrice": { "type": "number" },
    "minThreshold": { "type": "number" },
    "category": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Show me item abc123", "Get details for tomatoes"

---

### T-I4: `getInventoryByCategory`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get inventory items filtered by category |
| **Scope** | User-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "category": { "type": "string", "description": "Category to filter by" }
  },
  "required": ["category"]
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "currentStock": { "type": "number" },
      "unit": { "type": "string" },
      "unitPrice": { "type": "number" },
      "minThreshold": { "type": "number" }
    }
  }
}
```

**Example NL Prompts:** "Show me dairy items", "What produce do I have?"

---

### T-I5: `createInventoryItem`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Add a new inventory item |
| **Scope** | User-scoped (item assigned to current user) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "name": { "type": "string", "description": "Item name" },
    "currentStock": { "type": "number", "description": "Initial stock quantity" },
    "unit": { "type": "string", "description": "Unit of measure (e.g., kg, lbs, units)" },
    "unitPrice": { "type": "number", "description": "Price per unit" },
    "minThreshold": { "type": "number", "description": "Minimum stock threshold for alerts" },
    "category": { "type": "string", "description": "Item category" }
  },
  "required": ["name", "currentStock", "unit"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string", "description": "Generated item ID" },
    "name": { "type": "string" },
    "currentStock": { "type": "number" }
  }
}
```

**Example NL Prompts:** "Add tomatoes to inventory with 50 kg stock", "Create item Organic Milk, 100 units"

---

### T-I6: `updateStock`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Update the stock level of an inventory item |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "itemId": { "type": "string", "description": "The inventory item ID" },
    "newStock": { "type": "number", "description": "New stock quantity" }
  },
  "required": ["itemId", "newStock"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "name": { "type": "string" },
    "previousStock": { "type": "number" },
    "currentStock": { "type": "number" }
  }
}
```

**Example NL Prompts:** "Update tomatoes stock to 45 kg", "Set milk stock to 80 units"

---

### T-I7: `createStockTransaction`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Record a stock transaction (purchase, sale, or adjustment) |
| **Scope** | User-scoped (item ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "itemId": { "type": "string", "description": "The inventory item ID" },
    "quantityChange": { "type": "number", "description": "Quantity change (positive for purchase, negative for sale)" },
    "type": {
      "type": "string",
      "enum": ["PURCHASE", "SALE", "ADJUSTMENT"],
      "description": "Transaction type"
    },
    "documentId": { "type": "string", "description": "Optional linked document ID" }
  },
  "required": ["itemId", "quantityChange", "type"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "transactionId": { "type": "string" },
    "itemId": { "type": "string" },
    "itemName": { "type": "string" },
    "quantityChange": { "type": "number" },
    "type": { "type": "string" },
    "newStock": { "type": "number" },
    "timestamp": { "type": "string", "format": "date-time" }
  }
}
```

**Example NL Prompts:** "Record purchase of 20 kg tomatoes", "Log sale of 5 units of milk"

---

### T-I8: `deleteInventoryItem`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Remove an inventory item |
| **Scope** | User-scoped (ownership verified) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "itemId": { "type": "string", "description": "The inventory item ID to delete" }
  },
  "required": ["itemId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "deleted": { "type": "boolean" },
    "itemId": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Delete item abc123", "Remove tomatoes from inventory"

---

## 6. Tool Catalog — Orders

### T-O1: `getRecentOrders`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get the 10 most recent orders for the restaurant |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "orderDate": { "type": "string", "format": "date-time" },
      "totalAmount": { "type": "number" },
      "status": { "type": "string" },
      "itemCount": { "type": "integer" }
    }
  }
}
```

**Example NL Prompts:** "What are my recent orders?", "Show me the last 10 orders"

---

### T-O2: `getOrderById`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get a specific order with its items |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "orderId": { "type": "string", "description": "The order ID" }
  },
  "required": ["orderId"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "orderDate": { "type": "string", "format": "date-time" },
    "totalAmount": { "type": "number" },
    "status": { "type": "string" },
    "items": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "id": { "type": "string" },
          "menuItemName": { "type": "string" },
          "quantity": { "type": "integer" },
          "price": { "type": "number" }
        }
      }
    }
  }
}
```

**Example NL Prompts:** "Show me order abc123", "Get details for today's lunch order"

---

### T-O3: `getOrdersByDateRange`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get orders within a date range |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "startDate": { "type": "string", "format": "date", "description": "Start date (YYYY-MM-DD)" },
    "endDate": { "type": "string", "format": "date", "description": "End date (YYYY-MM-DD)" }
  },
  "required": ["startDate", "endDate"]
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "orderDate": { "type": "string", "format": "date-time" },
      "totalAmount": { "type": "number" },
      "status": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Show me orders from this week", "What orders did we get in January?"

---

### T-O4: `getOrdersByStatus`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get orders filtered by status |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "status": { "type": "string", "description": "Order status to filter by" }
  },
  "required": ["status"]
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "orderDate": { "type": "string", "format": "date-time" },
      "totalAmount": { "type": "number" },
      "status": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "Show me pending orders", "Which orders are completed?"

---

### T-O5: `createOrder`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Create a new order |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "totalAmount": { "type": "number", "description": "Total order amount" },
    "status": { "type": "string", "description": "Initial order status (default: PENDING)" },
    "items": {
      "type": "array",
      "description": "Order items",
      "items": {
        "type": "object",
        "properties": {
          "menuItemId": { "type": "string", "description": "Menu item ID" },
          "quantity": { "type": "integer", "description": "Quantity ordered" },
          "price": { "type": "number", "description": "Price per unit" }
        },
        "required": ["menuItemId", "quantity", "price"]
      }
    }
  },
  "required": ["totalAmount"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string", "description": "Generated order ID" },
    "orderDate": { "type": "string", "format": "date-time" },
    "totalAmount": { "type": "number" },
    "status": { "type": "string" },
    "itemCount": { "type": "integer" }
  }
}
```

**Example NL Prompts:** "Create an order for 2 burgers and 1 fries", "Place a new order"

---

### T-O6: `updateOrderStatus`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Update the status of an order |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "orderId": { "type": "string", "description": "The order ID" },
    "status": { "type": "string", "description": "New order status" }
  },
  "required": ["orderId", "status"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "string" },
    "status": { "type": "string" },
    "orderDate": { "type": "string", "format": "date-time" }
  }
}
```

**Example NL Prompts:** "Mark order abc123 as completed", "Update order status to preparing"

---

### T-O7: `addOrderItem`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Add an item to an existing order |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "orderId": { "type": "string", "description": "The order ID" },
    "menuItemId": { "type": "string", "description": "Menu item ID to add" },
    "quantity": { "type": "integer", "description": "Quantity" },
    "price": { "type": "number", "description": "Price per unit" }
  },
  "required": ["orderId", "menuItemId", "quantity", "price"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "orderId": { "type": "string" },
    "orderItemId": { "type": "string" },
    "newTotalAmount": { "type": "number" }
  }
}
```

**Example NL Prompts:** "Add 2 coffees to order abc123", "Add salad to the current order"

---

## 7. Tool Catalog — Menu Items

### T-M1: `getMenuItems`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | List all menu items for the restaurant |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "description": { "type": "string" },
      "price": { "type": "number" },
      "category": { "type": "string" }
    }
  }
}
```

**Example NL Prompts:** "What's on the menu?", "Show me all menu items"

---

### T-M2: `getMenuItemsByCategory`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get menu items filtered by category |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "category": { "type": "string", "description": "Menu category to filter by" }
  },
  "required": ["category"]
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "description": { "type": "string" },
      "price": { "type": "number" }
    }
  }
}
```

**Example NL Prompts:** "Show me appetizers", "What desserts do we have?"

---

## 8. Tool Catalog — Metrics & Restaurant

### T-R1: `getRestaurantMetrics`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get performance metrics for the restaurant |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "metricName": { "type": "string" },
      "metricValue": { "type": "number" },
      "periodStart": { "type": "string", "format": "date-time" },
      "periodEnd": { "type": "string", "format": "date-time" }
    }
  }
}
```

**Example NL Prompts:** "What are my restaurant metrics?", "Show me performance data"

---

## 9. Tool Catalog — Subscriptions

### T-S1: `getSubscriptionInfo`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get current subscription plan and usage for the restaurant |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "planName": { "type": "string" },
    "planPrice": { "type": "number" },
    "maxDocumentsSizeMb": { "type": "integer" },
    "maxChatsPerMonth": { "type": "integer" },
    "maxAccountsPerRestaurant": { "type": "integer" },
    "currentDocumentsSizeMb": { "type": "number" },
    "currentChatsCount": { "type": "integer" },
    "currentNotificationsCount": { "type": "integer" },
    "startDate": { "type": "string", "format": "date-time" },
    "endDate": { "type": "string", "format": "date-time" }
  }
}
```

**Example NL Prompts:** "What's my current subscription?", "Show me my plan limits and usage"

---

### T-S2: `checkSubscriptionLimit`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Check if an action is within subscription tier limits |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "actionType": {
      "type": "string",
      "enum": ["CHAT", "DOCUMENT_UPLOAD", "NOTIFICATION"],
      "description": "Type of action to check"
    }
  },
  "required": ["actionType"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "allowed": { "type": "boolean" },
    "actionType": { "type": "string" },
    "currentUsage": { "type": "integer" },
    "limit": { "type": "integer" },
    "remaining": { "type": "integer" }
  }
}
```

**Example NL Prompts:** "Am I within my chat limit?", "Can I upload another document?"

---

### T-S3: `incrementUsage`

| Field | Value |
|-------|-------|
| **Type** | Write |
| **Description** | Increment subscription usage counter (atomic operation) |
| **Scope** | Restaurant-scoped |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {
    "actionType": {
      "type": "string",
      "enum": ["CHAT", "DOCUMENT_UPLOAD", "NOTIFICATION"],
      "description": "Type of usage to increment"
    },
    "amount": {
      "type": "integer",
      "description": "Amount to increment (default: 1)",
      "default": 1
    }
  },
  "required": ["actionType"]
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "actionType": { "type": "string" },
    "newCount": { "type": "integer" },
    "monthYear": { "type": "string" }
  }
}
```

**Example NL Prompts:** "Increment chat usage", "Record a document upload for billing"

---

### T-S4: `getAllPlans`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get all available subscription plans (Free, Pro, Enterprise) |
| **Scope** | Restaurant-scoped |
| **Added** | T-4.10 (Phase 4) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": { "type": "string" },
      "name": { "type": "string" },
      "monthlyPrice": { "type": "number" },
      "documentLimit": { "type": "integer" },
      "aiTokensLimit": { "type": "integer" },
      "maxAccountsPerRestaurant": { "type": "integer" }
    }
  }
}
```

**Example NL Prompts:** "What plans are available?", "Show me subscription options"

---

### T-S5: `getAccountCount`

| Field | Value |
|-------|-------|
| **Type** | Read |
| **Description** | Get the number of user accounts associated with the current restaurant |
| **Scope** | Restaurant-scoped |
| **Added** | T-4.10 (Phase 4) |

**Input Schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": []
}
```

**Output Schema:**
```json
{
  "type": "object",
  "properties": {
    "count": { "type": "integer", "description": "Number of user accounts in the restaurant" }
  }
}
```

**Example NL Prompts:** "How many users are in my restaurant?", "What's my account count?"

---

## 10. Tool Summary Matrix

| # | Tool Name | Type | Domain | Scope | Replaces |
|---|-----------|------|--------|-------|----------|
| T-D1 | `getDocumentCount` | Read | Document | User | `DocumentService` |
| T-D2 | `getDocumentById` | Read | Document | User | `DocumentService` |
| T-D3 | `getDocumentsByStatus` | Read | Document | User | `DocumentService` |
| T-D4 | `getLastUploadedDocument` | Read | Document | User | `DocumentService` |
| T-D5 | `searchDocuments` | Read | Document | User | `DocumentService` |
| T-D6 | `createDocument` | Write | Document | User | `DocumentService` |
| T-D7 | `updateDocumentStatus` | Write | Document | User | `DocumentService` |
| T-D8 | `deleteDocument` | Write | Document | User | `DocumentService` |
| T-D9 | `getUserDocuments` | Read | Document | User | `DocumentService` |
| T-V1 | `getVendors` | Read | Vendor | User | `InventoryService` |
| T-V2 | `getVendorById` | Read | Vendor | User | `InventoryService` |
| T-V3 | `getVendorsByCategory` | Read | Vendor | User | `InventoryService` |
| T-V4 | `createVendor` | Write | Vendor | User | `InventoryService` |
| T-V5 | `updateVendor` | Write | Vendor | User | `InventoryService` |
| T-V6 | `deleteVendor` | Write | Vendor | User | `InventoryService` |
| T-I1 | `getInventoryItems` | Read | Inventory | User | `InventoryService` |
| T-I2 | `getLowStockItems` | Read | Inventory | User | `InventoryService` |
| T-I3 | `getInventoryItemById` | Read | Inventory | User | `InventoryService` |
| T-I4 | `getInventoryByCategory` | Read | Inventory | User | `InventoryService` |
| T-I5 | `createInventoryItem` | Write | Inventory | User | `InventoryService` |
| T-I6 | `updateStock` | Write | Inventory | User | `InventoryService` |
| T-I7 | `createStockTransaction` | Write | Inventory | User | `InventoryService` |
| T-I8 | `deleteInventoryItem` | Write | Inventory | User | `InventoryService` |
| T-O1 | `getRecentOrders` | Read | Order | Restaurant | `DashboardService` |
| T-O2 | `getOrderById` | Read | Order | Restaurant | `DashboardService` |
| T-O3 | `getOrdersByDateRange` | Read | Order | Restaurant | `DashboardService` |
| T-O4 | `getOrdersByStatus` | Read | Order | Restaurant | `DashboardService` |
| T-O5 | `createOrder` | Write | Order | Restaurant | `DashboardService` |
| T-O6 | `updateOrderStatus` | Write | Order | Restaurant | `DashboardService` |
| T-O7 | `addOrderItem` | Write | Order | Restaurant | `DashboardService` |
| T-M1 | `getMenuItems` | Read | Menu | Restaurant | `DashboardService` |
| T-M2 | `getMenuItemsByCategory` | Read | Menu | Restaurant | `DashboardService` |
| T-R1 | `getRestaurantMetrics` | Read | Metrics | Restaurant | `DashboardService` |
| T-S1 | `getSubscriptionInfo` | Read | Subscription | Restaurant | `SubscriptionService` |
| T-S2 | `checkSubscriptionLimit` | Write | Subscription | Restaurant | `SubscriptionService` |
| T-S3 | `incrementUsage` | Write | Subscription | Restaurant | `SubscriptionService` |
| T-S4 | `getAllPlans` | Read | Subscription | Restaurant | `SubscriptionService` |
| T-S5 | `getAccountCount` | Read | Subscription | Restaurant | `SubscriptionService` |

---

## 11. Implementation Notes

### 11.1 Class Structure

```java
package com.example.SuChefService.mcp;

@Component
@RequiredArgsConstructor
public class McpToolProvider {
    // All 12 repositories injected via constructor
    // getCurrentUser() + getCurrentRestaurant() (public for service delegation)
    // 38 @Tool methods organized by domain
    // Structured error responses: notFoundError(), accessDeniedError(), validationError()
}
```

### 11.2 User Resolution Pattern

All tools share the same user resolution logic (from `ChatTools.java:30-40`):

```java
private User getCurrentUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String email = (principal instanceof UserDetails)
        ? ((UserDetails) principal).getUsername()
        : principal.toString();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
}
```

### 11.3 Tool Registration

Tools are auto-registered by Spring AI when annotated with `@Tool`. The MCP server exposes them via `tools/list` endpoint. No manual registration needed.

### 11.4 Error Handling

Each tool should return structured error responses:
- **Not Found:** `{ "error": "Document not found", "documentId": "abc123" }`
- **Access Denied:** `{ "error": "Access denied", "reason": "Document belongs to another user" }`
- **Validation Error:** `{ "error": "Invalid status", "validValues": ["RECEIVED", "PROCESSING", ...] }`

### 11.5 Error Handling

All MCP tools return structured error responses via try-catch wrapping:
- **Not Found:** `{ "error": "NOT_FOUND", "resource": "Document", "id": "abc123", "message": "..." }`
- **Access Denied:** `{ "error": "ACCESS_DENIED", "resource": "Document", "id": "abc123", "message": "..." }`
- **Validation Error:** `{ "error": "VALIDATION_ERROR", "field": "status", "message": "Invalid status. Valid: ..." }`

Error helpers: `notFoundError()`, `accessDeniedError()`, `validationError()`.

---

## 12. Acceptance Criteria Checklist

- [x] Read tools: documents (6), vendors (3), inventory (4), orders (4), menu (2), metrics (1), subscriptions (3) = **23 read tools**
- [x] Write tools: documents (3), vendors (3), inventory (4), orders (3), subscriptions (2) = **15 write tools**
- [x] Domain tools: subscription checks, usage increments = **2 domain tools** (included in write count)
- [x] Each tool has JSON Schema input/output definition
- [x] Multi-tenant scoping documented per tool (User-scoped vs Restaurant-scoped)
- [x] Total: **38 tools** (exceeds 30+ requirement)

---

## 13. Next Task

**Phase 7: Documentation & Production Readiness — ✅ Complete**  
All documentation updated. MCP server production-ready with health checks, timeouts, rate limiting, and observability.

**Next:** Production deployment validation and monitoring setup.

---

### Completed Implementation Tasks

| Task | Status | Date | Notes |
|------|--------|------|-------|
| T-2.1: Document Read Tools (T-D1–T-D5) | Done | 2026-05-31 | 5 read tools + repository method |
| T-2.2: Vendor Read Tools (T-V1–T-V3) | Done | 2026-05-31 | 3 read tools + `findByUserAndCategory` |
| T-2.3: Vendor Write Tools (T-V4–T-V6) | Done | 2026-05-31 | 3 write tools |
| T-3.1: Inventory Read Tools (T-I1–T-I4) | Done | 2026-05-31 | 4 read tools + `findByUserAndCategory` |
| T-3.2: Inventory Write Tools (T-I5–T-I8) | Done | 2026-05-31 | 4 write tools (createInventoryItem, updateStock, createStockTransaction, deleteInventoryItem) |
| T-4.1: Order Read Tools (T-O1–T-O4) | Done | 2026-05-31 | 4 read tools (getRecentOrders, getOrderById, getOrdersByDateRange, getOrdersByStatus) + 2 OrderRepository methods + getCurrentRestaurant() helper |
| T-4.2: Order Write Tools (T-O5–T-O7) | Done | 2026-05-31 | 3 write tools (createOrder, updateOrderStatus, addOrderItem) + OrderItemInput DTO |
| T-5.1: Menu Read Tools (T-M1–T-M2) | Done | 2026-05-31 | 2 read tools (getMenuItems, getMenuItemsByCategory) + `MenuItemRepository.findByRestaurantAndCategory` added |
| T-5.2: Metrics Read Tools (T-R1) | Done | 2026-05-31 | 1 read tool (getRestaurantMetrics) + `RestaurantMetricInfo` DTO + mapper |
| T-6.1: Subscription Read Tool (T-S1) | Done | 2026-05-31 | 1 read tool (getSubscriptionInfo) + `SubscriptionInfo` DTO combining plan + usage |
| T-6.2: Subscription Write Tools (T-S2–T-S3) | Done | 2026-05-31 | 2 write tools (checkSubscriptionLimit, incrementUsage) + 2 DTOs (SubscriptionLimitCheckResponse, SubscriptionUsageIncrementResponse) + `@Transactional` on incrementUsage |
| T-7.1: Document Write Tools (T-D6–T-D8) | Done | 2026-05-31 | 3 write tools (createDocument, updateDocumentStatus, deleteDocument) + 3 DTOs (DocumentCreateResponse, DocumentUpdateStatusResponse, DocumentDeleteResponse) + `DocumentClassification` import |
| T-8.1: MCP Server Integration Testing & Evaluation | Done | 2026-05-31 | `McpToolProviderTest.java` — 48 tests covering all 35 tools across all 7 domains + `MenuItem` import fix to McpToolProvider |
| T-8.2: MCP Feature Flag Integration & Error Handling | Done | 2026-05-31 | Structured try-catch wrapping all 35 tools → error DTOs (NOT_FOUND, ACCESS_DENIED, VALIDATION_ERROR) + `McpToolProviderErrorHandlingTest` (15 tests) |
| T-4.10: Add Missing MCP Tools | Done | 2026-05-31 | 3 new tools: T-D9 (getUserDocuments), T-S4 (getAllPlans), T-S5 (getAccountCount) — total now 38 |
