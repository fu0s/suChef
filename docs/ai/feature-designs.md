# Feature Designs

This document outlines the core features of the SuChef platform, including their behavior, data contracts, and logical flows.

> **Updated (Phase 4–7):** All database interactions now flow through the MCP Server. Services delegate to `McpToolProvider` (38 tools). Direct repository calls from services are prohibited.

---

## 1. Authentication & Tenant Assignment
Allows users to register, log in, and manage their restaurant association (tenancy model).

### Flows
1. **Registration**: User registers an account with an email and password.
2. **Authentication**: User logs in and receives a JWT token.
3. **Tenant Selection**: User associates their account with a restaurant (creates one or joins one).

### API Contracts

#### POST `/api/auth/register`
* **Request Payload (`RegisterRequest`):**
  ```json
  {
    "email": "user@example.com",
    "password": "securepassword",
    "fullName": "John Doe"
  }
  ```
* **Response Payload (`AuthResponse`):**
  ```json
  {
    "token": "eyJhbGciOi...",
    "email": "user@example.com",
    "restaurantName": null
  }
  ```

#### POST `/api/auth/login`
* **Request Payload (`AuthRequest`):**
  ```json
  {
    "email": "user@example.com",
    "password": "securepassword"
  }
  ```
* **Response Payload (`AuthResponse`):**
  ```json
  {
    "token": "eyJhbGciOi...",
    "email": "user@example.com",
    "restaurantName": "My Bistro"
  }
  ```

#### POST `/api/auth/restaurant`
* **Request Payload:**
  ```json
  {
    "restaurantName": "My Bistro"
  }
  ```
* **Response Payload (`AuthResponse`):**
  ```json
  {
    "token": "eyJhbGciOi...",
    "email": "user@example.com",
    "restaurantName": "My Bistro"
  }
  ```

---

## 2. Dashboard Metrics
Provides a consolidated view of restaurant operations, including inventory, vendors, menu items, and recent document activity.

### Flows
1. Frontend requests metrics for the authenticated user's associated restaurant.
2. Backend queries multiple repositories (Menu, Inventory, Vendor, Order, Document) to construct metrics.
3. Frontend renders the summary stats and charts.

### API Contracts

#### GET `/api/dashboard/metrics`
* **Headers:** `Authorization: Bearer <token>`
* **Response Payload (`DashboardMetricsResponse`):**
  ```json
  {
    "totalMenuItems": 42,
    "totalInventoryItems": 150,
    "totalVendors": 12,
    "totalOrders": 85,
    "totalDocuments": 24,
    "lowStockCount": 5
  }
  ```

---

## 3. Document Ingestion & Processing
Supports uploading PDF invoices, receipts, and menu sheets. Files are classified, and information is extracted using AI. **All DB operations route through MCP tools.**

### Flows
1. User uploads a file (PDF or Image).
2. Backend persists the file locally under `/uploads/documents/` and calls `McpToolProvider.createDocument()` (T-D6) to record metadata (status = `PROCESSING`).
3. Document analysis service reads file content, invokes the AI model for entity extraction, and calls `McpToolProvider.updateDocumentStatus()` (T-D7) to set final status (`SUCCESS` / `FAILED`).
4. Extracted entities (vendors, orders, inventory) are saved via MCP write tools (T-V4, T-O5, T-I5).

### MCP Tools Used
| Tool | Operation | Phase |
|------|-----------|-------|
| T-D6: `createDocument` | Store document metadata | Upload |
| T-D7: `updateDocumentStatus` | Update status (PROCESSING → COMPLETED) | Analysis |
| T-D9: `getUserDocuments` | List all user documents | Frontend |
| T-D8: `deleteDocument` | Remove document | User action |
| T-V4: `createVendor` | Save extracted vendor | Analysis |
| T-O5: `createOrder` | Save extracted order | Analysis |
| T-I5: `createInventoryItem` | Save extracted inventory | Analysis |

### API Contracts

#### POST `/api/documents/upload`
* **Request:** `Multipart/Form-Data` with parameter `file`.
* **Response Payload (`Document`):**
  ```json
  {
    "id": "37826bbf-e637-40aa-9354-dc89df4f58b1",
    "fileName": "invoice_123.pdf",
    "filePath": "/uploads/documents/37826bbf-e637-40aa-9354-dc89df4f58b1.pdf",
    "fileSize": 1048576,
    "status": "PROCESSING",
    "classification": "INVOICE",
    "uploadedAt": "2026-05-25T15:30:00"
  }
  ```

#### GET `/api/documents`
* **Response Payload:** `List<Document>`

#### DELETE `/api/documents/{id}`
* **Response:** `204 No Content`

---

## 4. Subscription & Usage
Enforces tier-based limitations (Free, Pro, Enterprise) on features like monthly document uploads and AI tokens. **All DB operations route through MCP tools.**

### Flows
1. Frontend calls `McpToolProvider.getAllPlans()` (T-S4) to get available plans.
2. Frontend calls `McpToolProvider.getSubscriptionInfo()` (T-S1) for current plan + usage.
3. Before each action (chat, document upload), backend calls `McpToolProvider.checkSubscriptionLimit()` (T-S2).
4. After successful action, backend calls `McpToolProvider.incrementUsage()` (T-S3).

### MCP Tools Used
| Tool | Operation | Trigger |
|------|-----------|---------|
| T-S4: `getAllPlans` | List available plans | Frontend load |
| T-S1: `getSubscriptionInfo` | Current plan + usage | Frontend load |
| T-S2: `checkSubscriptionLimit` | Verify limits | Before action |
| T-S3: `incrementUsage` | Record usage | After action |
| T-S5: `getAccountCount` | Count users in restaurant | Admin view |

### API Contracts

#### GET `/api/subscription/plans`
* **Response Payload:** `List<SubscriptionPlan>`

#### GET `/api/subscription/current`
* **Response Payload (`RestaurantSubscription`):**
  ```json
  {
    "id": "sub_49df92",
    "status": "ACTIVE",
    "startDate": "2026-05-01T00:00:00",
    "endDate": "2026-06-01T00:00:00",
    "plan": {
      "id": "plan_pro",
      "name": "Pro",
      "monthlyPrice": 49.99,
      "documentLimit": 100,
      "aiTokensLimit": 500000
    }
  }
  ```

#### GET `/api/subscription/usage`
* **Response Payload (`SubscriptionUsage`):**
  ```json
  {
    "id": "usage_9281a",
    "monthYear": "05-2026",
    "documentsProcessed": 14,
    "aiTokensUsed": 45000
  }
  ```

---

## 5. Interactive AI Assistant (Chat & Voice)
Provides restaurant staff with real-time support for inventory management, vendor list retrieval, and recipes. **All data queries route through MCP tools via Spring AI ChatClient.**

### Flows
1. User sends message via chat interface.
2. Backend streams responses back using Server-Sent Events (SSE).
3. Spring AI ChatClient resolves tool calls through `McpToolProvider` (38 tools available).
4. Voice config returns active capabilities (browser-native speech synthesis/recognition vs server-side API).

### MCP Integration
The `ChatClient` bean (configured in `AiConfig`) uses `McpToolProvider` as its default tool set. When the LLM decides to call a tool (e.g., "How many documents do I have?"), it invokes `McpToolProvider.getDocumentCount()` (T-D1) automatically.

### Available MCP Tools for Chat
All 38 tools are available to the chat AI. Common patterns:
- **Document queries**: T-D1–T-D5, T-D9
- **Vendor lookups**: T-V1–T-V3
- **Inventory checks**: T-I1–T-I4
- **Order management**: T-O1–T-O4
- **Metrics**: T-R1
- **Subscription info**: T-S1, T-S4, T-S5

### API Contracts

#### GET `/api/ai/stream?message={message}`
* **Response Format:** `text/event-stream` containing chunks of Markdown text.

#### GET `/api/voice/config`
* **Response Payload:**
  ```json
  {
    "enabled": true,
    "provider": "browser-native"
  }
  ```

---

## 6. MCP REST API (External Client Support)
Exposes all 38 MCP tools as REST endpoints at `/api/mcp/*` for external client access (Angular frontend, third-party integrations).

### Endpoints Summary

| Domain | Endpoints | Method |
|--------|-----------|--------|
| Documents | `/api/mcp/documents/count`, `/{id}`, `/status/{status}`, `/last`, `/search`, `/list`, `/create`, `/{id}/status`, `/{id}/delete` | GET/POST |
| Vendors | `/api/mcp/vendors/list`, `/{id}`, `/category/{category}`, `/create`, `/{id}/update`, `/{id}/delete` | GET/POST |
| Inventory | `/api/mcp/inventory/list`, `/low-stock`, `/{id}`, `/category/{category}`, `/create`, `/{id}/stock`, `/{id}/transaction`, `/{id}/delete` | GET/POST |
| Orders | `/api/mcp/orders/recent`, `/{id}`, `/date-range`, `/status/{status}`, `/create`, `/{id}/status`, `/{id}/item` | GET/POST |
| Menu | `/api/mcp/menu/list`, `/category/{category}` | GET |
| Metrics | `/api/mcp/metrics` | GET |
| Subscription | `/api/mcp/subscription/info`, `/plans`, `/check`, `/increment`, `/accounts` | GET/POST |

### Authentication
All MCP endpoints require valid JWT via `Authorization: Bearer <token>` header.

### Rate Limiting
60 requests per minute per user via `McpRateLimitFilter` on `/mcp/**` endpoints.

### Error Responses
All errors follow the structure:
```json
{
  "error": "NOT_FOUND|ACCESS_DENIED|VALIDATION_ERROR",
  "resource": "Document",
  "id": "abc123",
  "message": "..."
}
```
