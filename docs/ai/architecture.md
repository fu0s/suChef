# SuChef — Architecture Document
> Owner: `architect` | Last updated: 2026-06-01

---

## 1. System Overview

SuChef is a multi-tenant SaaS platform for restaurant management. It consists of:

| Component | Technology | Port |
|-----------|-----------|------|
| Backend API | Spring Boot 3.4 / Java 21 | 8080 |
| Frontend SPA | Angular 20 | 4200 |
| Database (prod) | MySQL 8.3 | 3306 |
| Database (test) | H2 (in-memory) | — |
| Schema migrations | Liquibase | — |

```
┌──────────────────────────────────────────────────────────────┐
│                    Browser / Angular SPA                      │
│  suchef/src/app/**                                           │
│  (Auth, Dashboard, Documents, Chat, Subscriptions)           │
└────────────────────────┬─────────────────────────────────────┘
                         │  HTTP/REST + SSE
                         ▼
┌──────────────────────────────────────────────────────────────┐
│              Spring Boot API  (port 8080)                     │
│                                                               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                    │
│  │Controllers│→ │ Services │→ │  MCP     │                    │
│  └──────────┘  └──────────┘  │  Server  │                    │
│                               │(38 tools)│                    │
│                               └────┬─────┘                    │
│                                    │                          │
│                               ┌────▼─────┐                    │
│                               │  JPA     │                    │
│                               │Repositories│                  │
│                               └────┬─────┘                    │
│                                    │                          │
│                               ┌────▼─────┐                    │
│                               │Spring AI │                    │
│                               │(Ollama)  │                    │
│                               └──────────┘                    │
└────────────────────────┬─────────────────────────────────────┘
                         │  JDBC / Liquibase
                         ▼
                ┌──────────────────┐
                │  MySQL / H2      │
                │  (17 tables)     │
                └──────────────────┘
```

> **Note:** As of Phase 4 (EPIC-MCP-001), all database interactions flow through the MCP Server. Services delegate to `McpToolProvider` which exposes 38 tools. Direct repository calls from services are prohibited.

---

## 2. Backend Module Boundaries

### 2.1 Package Structure
```
com.example.SuChefService/
├── controller/      — REST API surface (HTTP in/out only, no logic)
├── service/         — Business logic, AI integrations, file handling
├── mcp/             — MCP Tool Provider (38 tools, single DB gateway)
├── repository/      — Spring Data JPA interfaces (used ONLY by MCP tools)
├── entity/          — JPA-managed DB entities
├── dto/             — API request/response contracts (never expose entities)
├── security/        — JWT filter, token util
├── config/          — Spring beans (Security, CORS, AI, Auth, MCP rate limiter)
├── exception/       — Global exception handler
└── tool/            — (deprecated) Former ChatTools — removed in Phase 6
```

### 2.2 Layer Contracts
- **Controller → Service**: Controllers must only call services; no repository access.
- **Service → MCP**: Services delegate all DB interactions to `McpToolProvider` methods. Direct repository calls from services are **prohibited** (Phase 4 migration complete).
- **MCP → Repository**: `McpToolProvider` is the **single data access gateway**. All 38 tools own all persistence logic via JPA repositories.
- **Entity ↛ API**: Entities must never be returned directly from controllers. Use DTOs.
  > ⚠️ **Violations:**
  > - `SubscriptionController` returns raw `RestaurantSubscription`, `SubscriptionUsage`, and `SubscriptionPlan` entities.
  > - `DocumentController` returns the raw `Document` entity.

---

## 3. Frontend Module Boundaries

### 3.1 Feature Modules (lazy-loaded)
```
suchef/src/app/
├── features/
│   ├── home/          — Landing page (hero, pricing, features, contact)
│   ├── auth/          — Login, Register
│   ├── dashboard/     — Metrics and analytics view
│   ├── chat/          — AI assistant (streaming)
│   ├── documents/     — Upload, list, delete documents
│   ├── subscription/  — Plan and usage display
│   └── profile/       — User profile
├── shared/
│   ├── components/    — Reusable UI (Navbar, Sidebar, Button, Modal…)
│   ├── pipes/         — TranslatePipe (i18n)
│   └── directives/    — ClickOutside
└── core/
    ├── services/      — Auth, Chat, Documents, Voice, Subscription, Translation
    └── interceptors/  — AuthInterceptor (attaches JWT to outgoing requests)
```

### 3.2 Routing
- Auth-guarded routes (Dashboard, Chat, Documents, Subscription, Profile)
- Public routes (Home/Landing, Login, Register)
- Auth state managed via `AuthService` (stores JWT in `localStorage`)

---

## 4. Authentication & Security

- **Mechanism**: Stateless JWT (HS256, 10-hour expiry) via `JwtUtil`
- **Flow**:  
  `POST /api/auth/register` or `/login` → JWT returned → stored in localStorage → `AuthInterceptor` appends `Authorization: Bearer <token>` to every request
- **Filter**: `JwtAuthenticationFilter` validates token on each request before the Spring Security chain
- **CORS**: Configured in `SecurityConfig` — allows `localhost:4200` (dev only)
- **CSRF**: Disabled (stateless API, no session cookies)
- **Public Endpoints**: `/api/auth/**`, `/error`, async dispatchers

---

## 5. Database Schema (17 Changesets via Liquibase)

| Table | Key Relationships |
|-------|------------------|
| `users` | FK → `restaurants` |
| `restaurants` | Root tenant entity |
| `documents` | FK → `users` |
| `vendors` | FK → `restaurants` |
| `menu_items` | FK → `restaurants` |
| `inventory_items` | FK → `restaurants` |
| `orders` / `order_items` | FK → `restaurants` |
| `stock_transactions` | FK → `inventory_items` |
| `restaurant_metrics` | FK → `restaurants` |
| `subscription_plans` | Seed data (Free / Pro / Enterprise) |
| `restaurant_subscriptions` | FK → `restaurants`, `subscription_plans` |
| `subscription_usage` | FK → `restaurants`; unique (restaurant_id, month_year) |

**Migration files:**
- `db.changelog-master.yaml` — users, documents
- `db.changelog-1.1.yaml` — restaurants, vendors, menus, orders, inventory, metrics
- `db.changelog-1.2.yaml` — restructures user↔restaurant relationship (user owns restaurant_id)
- `db.changelog-1.3.yaml` — document size column, subscription tables, seed plans
- `db.changelog-1.4.yaml` — custom SQL (likely index or data backfill)

---

## 6. Key ADRs (Architecture Decision Records)

### ADR-001: JWT over Sessions
**Decision**: Use stateless JWT tokens.  
**Rationale**: Enables horizontal scaling without shared session store. Fits SSE streaming requirements.  
**Trade-off**: No server-side token revocation; mitigated by short expiry (10h).

### ADR-002: Single-Tenant Database Schema (Multi-Tenant via Restaurant ID)
**Decision**: All entities carry a `restaurant_id` foreign key rather than separate database schemas per tenant.  
**Rationale**: Simpler operations and migrations for current scale.  
**Trade-off**: Requires careful query filtering — missed restaurant_id joins would leak cross-tenant data.

### ADR-003: Liquibase for Schema Management
**Decision**: Use Liquibase with YAML changeset files.  
**Rationale**: Versioned, repeatable schema migrations with rollback support.  
**Trade-off**: Auto-run on startup can cause lock contention in clustered deployments.

### ADR-004: Server-Sent Events for AI Streaming
**Decision**: Use `text/event-stream` (SSE) over WebSockets for the AI chat endpoint.  
**Rationale**: One-way server-push is sufficient; SSE is simpler and HTTP/2 compatible.  
**Trade-off**: Cannot push proactive notifications from server → client outside the chat context.

### ADR-005: Local Filesystem for Document Storage
**Decision**: Documents are stored under `uploads/documents/` on the server filesystem.  
**Rationale**: Zero infrastructure overhead for initial development.  
**Trade-off**: Not suitable for containerized/clustered deployments; must migrate to object storage before production.

### ADR-006: Spring AI (Gemini) for AI Features
**Decision**: Use Spring AI's Gemini integration for chat and document analysis.  
**Rationale**: Native Spring integration; supports streaming and function calling.  
**Trade-off**: Vendor lock-in to Gemini; rate limits exposed to users during quota exhaustion.

### ADR-007: Natural Language Data Access via MCP Server
**Decision**: All database interactions flow through MCP (Model Context Protocol) server tools — no direct repository calls from services.  
**Rationale**: Unified data access pattern, AI-native architecture, protocol standard adopted by Anthropic/OpenAI/Google/Microsoft, extensible tool catalog.  
**Trade-off**: LLM latency on every DB call (+200–500ms), non-deterministic tool selection, increased testing complexity.  
**Mitigation**: Structured tool schemas constrain LLM output; deterministic tool selection via system prompts; feature flag `suChef.mcp.enabled` for rollback.  
**Scope**: 38 MCP tools covering Documents (9), Vendors (6), Inventory (8), Orders (7), Menu (2), Metrics (1), Subscription (5).  
**Verified**: Multi-tenant isolation verified across all 38 tools; security audit passed (T-9.1/T-9.2).

### ADR-008: Transaction Boundaries for MCP Tool Methods
**Decision**: All MCP tool methods that access entity relationships with lazy collections MUST use `@Transactional(readOnly = true)`. Repository methods that need lazy collections MUST use `@EntityGraph` or `JOIN FETCH`.  
**Rationale**: With `spring.jpa.open-in-view=false` (B-008), Hibernate Sessions are closed after repository calls. MCP tool methods run outside HTTP request lifecycle (dispatched by Spring AI on worker threads), so OSIV cannot help. Without explicit `@Transactional`, lazy proxy access throws `LazyInitializationException` (B-012).  
**Trade-off**: Adds transaction overhead per tool call (~1-5ms); mitigated by `readOnly=true` which skips dirty checking.  
**Pattern**: 
```java
@Tool(description = "...")
@Transactional(readOnly = true)
public Object getRecentOrders() {
    // Session stays open through mapper calls
    return orderRepository.findTop10By...(restaurant).stream()
            .map(this::mapToOrderSummaryInfo)  // safe: Session open
            .collect(Collectors.toList());
}
```
**Scope**: Applies to all 38 MCP tools that read entity relationships. Write tools inherit transaction from repository `save()` calls.

---

## 7. Production Readiness Gaps

| Gap | Priority | Status |
|-----|----------|--------|
| Local filesystem storage → Cloud Object Storage | 🔴 Critical | Open |
| No JWT refresh token / revocation | 🟠 High | Open |
| ~~CORS allows any localhost port~~ | 🟠 High | **Fixed** (T-9.2) — restricted via `${cors.allowed-origins}` |
| ~~No AI rate-limit resilience (circuit breaker)~~ | 🟠 High | **Partial** — MCP rate limiter (60 req/min) added (T-9.2); Resilience4j circuit breakers still needed |
| ~~No API rate limiting~~ | 🟡 Medium | **Fixed** (T-9.2) — `McpRateLimitFilter` on `/mcp/**` |
| Liquibase auto-run in clustered deploy | 🟡 Medium | Open |
| No HTTPS / TLS configuration | 🟡 Medium | Open |
| ~~JWT secret hardcoded~~ | 🔴 Critical | **Fixed** (T-9.2) — externalized to `${JWT_SECRET}` |
| ~~Empty DB password~~ | 🔴 Critical | **Fixed** (T-9.2) — externalized to `${DB_PASSWORD}` |

### 7.1 MCP Server Production Readiness (Phase 7)

| Component | Status | Notes |
|-----------|--------|-------|
| Health check endpoint | ✅ Added | `/actuator/health` includes MCP server status |
| Timeout configuration | ✅ Added | `spring.ai.mcp.server.request-timeout=30s` |
| Graceful degradation | ✅ Added | Structured error responses when LLM unavailable |
| Rate limiting | ✅ Added | 60 req/min per user via `McpRateLimitFilter` |
| Observability | ✅ Added | Tool call metrics via Spring Actuator + structured logging |
| Connection limits | ✅ Added | Configurable via `spring.ai.mcp.server.max-connections` |
