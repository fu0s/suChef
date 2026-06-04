# Known Issues & Edge Cases
> Owner: `qa-reviewer` | Last updated: 2026-06-01  
> Tests run: `mvn clean test` → ✅ 102/102 passed  

---

## Severity Legend
| Level | Description |
|-------|-------------|
| 🔴 Critical | Exploit or data loss risk in production |
| 🟠 High | Functional failure under real-world conditions |
| 🟡 Medium | Degraded UX or correctness issues |
| 🟢 Low | Code quality / maintainability |

---

## Architecture & Design Issues

> Extracted from knowledge graph analysis (2026-05-30). These are structural patterns, not bugs.

### [A-001] 🟠 AuthService God Node — Tight Coupling Across 15+ Components
**Files:** `auth.service.ts`, `auth.service.ts` (Angular)  
**Symptom:** `AuthService` has 26 edges in the knowledge graph — the highest of any node. It is injected directly by Dashboard, Chat, Profile, Sidebar, Header, Navbar, Landing, and every feature component. Changes to AuthService (e.g., adding a field, changing the token format) force recompilation and testing of the entire app.  
**Fix:** Extract an `AuthContext` or `CurrentUser` state service (e.g., a BehaviorSubject-based store) that components can read from without injecting AuthService directly. AuthService handles login/logout/token-refresh; AuthContext exposes `user$`, `isAuthenticated$`, `currentRestaurant$` as read-only observables. This reduces the blast radius of auth changes from 15+ components to just the state service.

---

### [A-002] 🟠 DocumentsService Shared by Dashboard, Documents, and Chat
**Files:** `documents.service.ts` (Angular), `DocumentService.java` (backend)  
**Symptom:** Three features depend on the same `DocumentsService`: Dashboard (metrics), Documents (upload/view), and Chat (document context). A change to document handling (e.g., adding a new field, changing the response format) ripples to all three features. The graph shows `DocumentsService` connected to `DashboardComponent`, `DocumentsUploadComponent`, and `ChatComponent`.  
**Fix:** Split into two services: `DocumentStorageService` (upload, download, list) and `DocumentAnalysisService` (AI processing, metrics). Dashboard uses AnalysisService, Documents uses StorageService, Chat uses both. Features only depend on what they actually need.

---

### [A-003] 🟡 ChatService Coupled to Subscription and Auth Stacks
**Files:** `chat.service.ts` (Angular), `ChatService.java` (backend)  
**Symptom:** Chat feature (Community 11, 30 nodes) depends on `SubscriptionService` for usage limits and `AuthService` for tokens. This means chat cannot be used independently — e.g., for demos, free-tier previews, or standalone testing. The graph shows `ChatComponent` → `ChatService` → `SubscriptionService` as a hard dependency chain.  
**Fix:** Inject a `ChatConfig` that abstracts subscription checks. For free/demo mode, `ChatConfig` returns `unlimited: true`. This decouples chat from the subscription stack and enables standalone demo mode.

---

### [A-004] 🟡 i18n Flat Key Structure Creates False Graph Density
**Files:** `suchef/public/i18n/de.json`, `suchef/public/i18n/en.json`  
**Symptom:** The `landing` i18n key has degree 18 and connects to 14 communities in the graph — the highest centrality score. This is an artifact of flat key structure (`landing.hero`, `landing.features`, `landing.pricing`, `landing.testimonials`) making the graph see false cross-feature connections. Real architectural bridges are obscured by i18n noise.  
**Fix:** Restructure i18n to feature-scoped namespaces: `landing.hero.*`, `dashboard.charts.*`, `auth.login.*`. This reduces graph noise and makes dependency tracking meaningful. Also improves maintainability — each feature owns its translations.

---

### [A-005] 🟡 994 Isolated Nodes Are Graph Noise, Not Code Issues
**File:** Various — mostly `angular.json`, `package.json`, i18n files  
**Symptom:** 1,156 nodes have degree ≤ 1 in the graph. Breakdown: ~43 config properties (`$schema`, `version`, `budgets`), ~600 i18n leaf keys (`title`, `subtitle`), ~200 AST method stubs (`.constructor()`, `.ngOnInit()`), ~50 test blocks. These inflate node count without representing architectural concepts.  
**Fix:** When querying the graph, filter by `file_type` or minimum degree (≥2) to reduce noise. For future extractions, consider collapsing JSON properties into their parent node and merging method stubs into their class node.

---

### [A-006] 🟢 Landing Page Eagerly Imports 6+ Sub-Components
**File:** `landing-new.component.ts`  
**Symptom:** `LandingNewComponent` directly imports `HeroSectionComponent`, `FeaturesGridComponent`, `PricingSectionComponent`, `RecommendationsCarouselComponent`, `ContactFormComponent`, and `AiSectionComponent`. All are loaded eagerly, even though only the hero and features are visible on initial render. The graph shows this as a hub node connecting to 6 separate component communities.  
**Fix:** Lazy-load below-the-fold sections (Pricing, Testimonials, Contact, AI) using Angular's dynamic component loading or `@defer` blocks. This improves initial load time and reduces the coupling surface.
