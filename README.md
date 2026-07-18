# SuChef — Multi-Tenant Restaurant Management SaaS

A production-ready, multi-tenant SaaS platform for restaurant management built with **Spring Boot 3.4** (Java 21) and **Angular 20**. Features AI-powered document processing with OCR, real-time streaming chat assistance, subscription management with usage tracking, and JWT-based authentication.

---

## 🎯 Business Overview

SuChef is designed for **independent restaurant owners and small chains** who need an all-in-one digital operations platform without the complexity and cost of enterprise ERPs. The platform addresses the daily operational pain points that consume hours of manual work:

### Core Problem Areas Solved

| Pain Point | SuChef Solution |
|------------|-----------------|
| **Paper invoices piling up** | Upload PDFs/images → automatic OCR extraction → structured vendor data |
| **No real-time visibility into costs** | Dashboard with revenue, COGS, profit margins, top dishes |
| **Inventory waste & stockouts** | Low-stock alerts, stock movement tracking, waste reduction metrics |
| **Menu pricing guesswork** | Ingredient costing per dish, margin analysis, price optimization suggestions |
| **Subscription fatigue from multiple tools** | Single platform: documents, chat AI, dashboard, subscriptions |
| **Staff knowledge gaps** | AI assistant answers operational questions using your actual data |

### Target Users

- **Owner/Operators** — Full access to dashboard, documents, chat, subscriptions
- **Managers** — Operational access (inventory, orders, vendors, chat)
- **Accountants/Bookkeepers** — Document access for expense reconciliation

### Value Proposition

- **Time savings**: 10+ hrs/week automated (invoice entry, inventory counts, reporting)
- **Cost reduction**: 15-20% food waste reduction via tracking & alerts
- **Better decisions**: Real-time margins, dish profitability, vendor performance
- **Scalable pricing**: Free tier for single location, Pro/Enterprise for chains

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Browser / Angular SPA                       │
│  suchef/src/app/**  (Auth, Dashboard, Chat, Documents, Profile) │
└────────────────────────────┬────────────────────────────────────┘
                             │  HTTP/REST + SSE (port 4200)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot API (port 8080)                   │
│  Controllers → Services → JPA Repositories → MySQL/H2           │
│                                                                  │
│  ┌────────────┐  ┌────────────┐  ┌─────────────────────────┐   │
│  │ Controllers│→ │  Services  │→ │  Spring Data JPA Repos  │   │
│  └────────────┘  └────────────┘  └─────────────────────────┘   │
│       │              │                    │                      │
│       │              │                    ▼                      │
│       │              │            ┌──────────────────┐           │
│       │              │            │  QueryRouter     │           │
│       │              │            │  (chat data      │           │
│       │              │            │   access layer)  │           │
│       │              │            └──────────────────┘           │
│       │              │                    │                      │
│       ▼              ▼                    ▼                      │
│  REST + SSE    Business Logic      Database (17 tables)          │
└────────────────────────────┬────────────────────────────────────┘
                             │  JDBC / Liquibase
                             ▼
                    ┌──────────────────┐
                    │  MySQL 8.3 / H2  │
                    │   (17 tables)    │
                    └──────────────────┘
```

### Key Architectural Decisions

- **Standard Layered Architecture**: Controllers → Services → JPA Repositories
- **Entity ↔ API Separation**: Controllers return DTOs only; JPA entities never leak to API layer
- **Stateless JWT Auth**: HS256, 10-hour expiry, stored in localStorage, attached via `AuthInterceptor`
- **Multi-Tenancy**: `Restaurant` is the root tenant entity; all business data scopes to `restaurant_id`
- **AI Integration**: Spring AI (Ollama) for chat, PDFBox + Tesseract for document OCR
- **Chat Data Access**: `QueryRouter` + `IntentClassifier` provide structured data to LLM

---

## 🛠 Tech Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Backend** | Spring Boot | 3.4.x |
| **Language** | Java | 21 (LTS) |
| **Build** | Maven | 3.9+ |
| **Database** | MySQL | 8.3 (prod) / H2 (test) |
| **Migrations** | Liquibase | 4.29+ |
| **AI** | Spring AI + Ollama | 1.0.0-M6 |
| **PDF/OCR** | Apache PDFBox, Tesseract | 2.0.30 / 5.3.4 |
| **Frontend** | Angular | 20.x |
| **Language** | TypeScript | 5.x |
| **Styling** | SCSS + CSS Variables | — |
| **State** | Signals + RxJS | — |
| **HTTP** | HttpClient + Interceptors | — |

---

## 📁 Repository Structure

```
suChef/
├── SuChefService/          # Spring Boot Backend
│   ├── src/main/java/com/example/SuChefService/
│   │   ├── controller/     # REST endpoints (Auth, Chat, Documents, Dashboard, Subscription, Voice)
│   │   ├── service/        # Business logic, AI, file handling, subscription limits
│   │   ├── chat/           # IntentClassifier, QueryRouter, usage policies
│   │   ├── repository/     # Spring Data JPA interfaces (used directly by services)
│   │   ├── entity/         # JPA entities (17 tables)
│   │   ├── dto/            # API request/response contracts
│   │   ├── security/       # JWT filter, token util, security config
│   │   ├── config/         # Spring beans (CORS, AI, Auth, OpenAPI)
│   │   ├── document/       # Upload validators (subscription-aware)
│   │   └── exception/      # Global exception handler
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/changelog/   # Liquibase migrations (17 changesets)
│   ├── pom.xml
│   └── Dockerfile
│
├── suchef/                 # Angular 20 Frontend
│   ├── src/app/
│   │   ├── core/
│   │   │   ├── services/   # Auth, Chat, Documents, Voice, Subscription, Translation
│   │   │   └── interceptors/  # AuthInterceptor (JWT attachment)
│   │   ├── shared/
│   │   │   ├── components/ # Navbar, Sidebar, Button, Modal...
│   │   │   ├── pipes/      # TranslatePipe (i18n)
│   │   │   └── directives/ # ClickOutside
│   │   └── features/       # Feature modules (lazy-loaded)
│   ├── angular.json
│   ├── package.json
│   ├── Dockerfile
│   └── nginx.conf
│
├── docs/ai/                # Architecture & feature design docs
│   ├── architecture.md
│   ├── feature-designs.md
│   └── known-issues.md
│
├── graphify-out/           # Knowledge graph (auto-generated)
│   ├── graph.json
│   ├── GRAPH_REPORT.md
│   └── graph.html
│
├── docker-compose.yml
└── README.md               # This file
```

---

## 🐳 Docker Deployment

### Quick Start with Docker Compose

```bash
# Build and start all services
docker compose up --build -d

# Pull AI model in Ollama container
docker compose exec ollama ollama pull llama3.1
```

### Services

| Service | Port | Description |
|---------|------|-------------|
| **Frontend** | 4200 | Angular app served via Nginx |
| **Backend** | 8080 | Spring Boot REST API |
| **MySQL** | 3306 | Primary database |
| **Ollama** | 11434 | Local LLM for AI chat |

### Docker Files Created

- `SuChefService/Dockerfile` — Multi-stage build with Tesseract OCR pre-installed
- `suchef/Dockerfile` — Node.js build → Nginx production serve
- `suchef/nginx.conf` — Reverse proxy config with `/api/*` forwarding to backend
- `docker-compose.yml` — Full stack with health checks and GPU support for Ollama

---

## 📚 API Reference

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new user + restaurant |
| `POST` | `/api/auth/login` | Login, returns JWT |
| `GET` | `/api/auth/me` | Get current user profile |

**Request (register):**
```json
{
  "email": "owner@restaurant.com",
  "password": "securePass123",
  "restaurantName": "Bella Italia",
  "ownerName": "Mario Rossi"
}
```

**Response (login):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": { "id": 1, "email": "owner@restaurant.com", "name": "Mario Rossi" },
  "restaurant": { "id": 1, "name": "Bella Italia" }
}
```

### Chat (AI Assistant)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat/message` | Send message (returns JSON) |
| `POST` | `/api/chat/stream` | Send message (streaming SSE) |
| `GET` | `/api/chat/history` | Get conversation history |

**SSE Stream Format:**
```
data: {"type":"token","content":"Hello"}
data: {"type":"token","content":" there"}
data: {"type":"done","messageId":42}
```

### Documents

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/documents/upload` | Upload PDF/image (multipart) |
| `GET` | `/api/documents` | List user documents |
| `GET` | `/api/documents/{id}` | Get document with extracted text |
| `DELETE` | `/api/documents/{id}` | Delete document |

### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/dashboard/metrics` | Revenue, orders, inventory alerts |
| `GET` | `/api/dashboard/revenue` | Time-series revenue data |

### Subscriptions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/subscription/current` | Current plan + usage |
| `GET` | `/api/subscription/plans` | Available plans |
| `GET` | `/api/subscription/usage` | Current month usage |

---

## 🗄 Database Schema (Liquibase)

17 changesets creating 17 tables:

| Table | Purpose | Key FKs |
|-------|---------|---------|
| `users` | System users | → `restaurants` |
| `restaurants` | Root tenant entity | — |
| `documents` | Uploaded PDFs + OCR text | → `users` |
| `vendors` | Supplier directory | → `restaurants` |
| `menu_items` | Restaurant menu | → `restaurants` |
| `menu_item_ingredients` | Recipe ingredients | → `menu_items` |
| `inventory_items` | Stock tracking | → `restaurants` |
| `orders` / `order_items` | Sales records | → `restaurants` |
| `stock_transactions` | Inventory audit log | → `restaurants`, `inventory_items` |
| `restaurant_metrics` | Aggregated metrics | → `restaurants` |
| `subscription_plans` | Plan definitions (Free/Pro/Enterprise) | — |
| `restaurant_subscriptions` | Active subscriptions | → `restaurants`, `subscription_plans` |
| `subscription_usage` | Monthly usage tracking | → `restaurants`; unique (`restaurant_id`, `month_year`) |
| `chat_sessions` / `chat_messages` | AI conversations | → `users` |

Run migrations:
```bash
mvn liquibase:update
```

---

## 🧪 Testing

### Backend

```bash
# Unit + integration tests
mvn test

# Specific test class
mvn test -Dtest=DocumentServiceTest

# With coverage (JaCoCo)
mvn test jacoco:report
# Report at target/site/jacoco/index.html
```

### Frontend

```bash
cd suchef

# Unit tests (Karma + Jasmine)
ng test

# E2E tests (Playwright - configure first)
ng e2e

# Lint
ng lint
```

---

## 🔐 Security Checklist (Production)

- [ ] Rotate `jwt.secret` (32+ char base64)
- [ ] Enable HTTPS/TLS (Let's Encrypt / cert-manager)
- [ ] Restrict CORS to production domain only
- [ ] Configure rate limiting (Bucket4j / Cloudflare)
- [ ] Store secrets in Vault / AWS Secrets Manager / Kubernetes Secrets
- [ ] Enable MySQL SSL: `useSSL=true&verifyServerCertificate=true`
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` in prod
- [ ] Run OWASP dependency check: `mvn org.owasp:dependency-check-maven:check`

---

## 📦 Key Dependencies

### Backend (pom.xml highlights)

```xml
<!-- Core -->
<dependency> org.springframework.boot:spring-boot-starter-web </dependency>
<dependency> org.springframework.boot:spring-boot-starter-data-jpa </dependency>
<dependency> org.springframework.boot:spring-boot-starter-security </dependency>
<dependency> org.springframework.boot:spring-boot-starter-validation </dependency>
<dependency> org.springframework.boot:spring-boot-starter-actuator </dependency>

<!-- Database -->
<dependency> com.mysql:mysql-connector-j </dependency>
<dependency> org.liquibase:liquibase-core </dependency>

<!-- AI -->
<dependency> org.springframework.ai:spring-ai-starter-model-ollama </dependency>

<!-- PDF / OCR -->
<dependency> org.apache.pdfbox:pdfbox:2.0.30 </dependency>
<dependency> org.bytedeco:tesseract-platform:5.3.4-1.5.10 </dependency>

<!-- JWT -->
<dependency> io.jsonwebtoken:jjwt-api:0.12.5 </dependency>
<dependency> io.jsonwebtoken:jjwt-impl:0.12.5 </dependency>
<dependency> io.jsonwebtoken:jjwt-jackson:0.12.5 </dependency>

<!-- Utilities -->
<dependency> org.projectlombok:lombok </dependency>
<dependency> org.mapstruct:mapstruct:1.5.5.Final </dependency>
```

### Frontend (package.json highlights)

```json
{
  "@angular/core": "^20.0.0",
  "@angular/common": "^20.0.0",
  "@angular/router": "^20.0.0",
  "@angular/forms": "^20.0.0",
  "rxjs": "^7.8.0",
  "zone.js": "^0.14.0",
  "typescript": "^5.4.0"
}
```

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

*Generated with ❤️ by the SuChef team • Last updated: July 2026*