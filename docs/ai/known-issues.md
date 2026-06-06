# Known Issues & Edge Cases
> Owner: `qa-reviewer` | Last updated: 2026-06-06  
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

### [A-001] 🟠 AuthService God Node — Tight Coupling Across 8+ Components
**Files:** `frontend/src/app/core/services/auth.service.ts`, `frontend/src/app/features/auth/components/login.component.ts`, `register.component.ts`, `dashboard.component.ts`, `profile.component.ts`, `sidebar.component.ts`, `navbar.component.ts`, `header.component.ts`, `app.ts`  
**Symptom:** `AuthService` is injected directly by 8+ components: Login, Register, Dashboard, Profile, Sidebar, Navbar, Header, and App root. Changes to AuthService (e.g., adding a field, changing the token format) force recompilation and testing of the entire app.  
**Fix:** Extract an `AuthContext` or `CurrentUser` state service (e.g., a BehaviorSubject-based store) that components can read from without injecting AuthService directly. AuthService handles login/logout/token-refresh; AuthContext exposes `user$`, `isAuthenticated$`, `currentRestaurant$` as read-only observables. This reduces the blast radius of auth changes from 8+ components to just the state service.

---

### [A-002] 🟡 DocumentsService Backend Coupling to SubscriptionService
**Files:** `SuChefService/src/main/java/com/example/SuChefService/service/DocumentService.java`  
**Symptom:** Backend `DocumentService` depends on `SubscriptionService` for document size limits and usage tracking (lines 41, 97, 124). Frontend `DocumentsService` is used by Dashboard and Documents upload only (Chat no longer uses it post-MCP removal). A change to document handling ripples to subscription logic.  
**Fix:** Decouple document upload validation from subscription checks. Use a `DocumentUploadValidator` interface that can be implemented differently per subscription tier, or move limit checks to a separate service layer.

---

### [A-003] 🟠 ChatService Coupled to SubscriptionService (Backend)
**Files:** `SuChefService/src/main/java/com/example/SuChefService/service/ChatService.java`  
**Symptom:** Backend ChatService hard-depends on `SubscriptionService` for restaurant resolution, chat limit checks, and usage incrementing (lines 20, 37-38, 76-78). This means chat cannot be used independently — e.g., for demos, free-tier previews, or standalone testing.  
**Fix:** Inject a `ChatConfig` or `UsagePolicy` abstraction that encapsulates subscription checks. For free/demo mode, the policy returns `unlimited: true`. This decouples chat from the subscription stack and enables standalone demo mode.

---

### [A-004] 🟡 i18n Flat Key Structure Creates False Graph Density
**Files:** `frontend/public/i18n/de.json`, `frontend/public/i18n/en.json`  
**Symptom:** The `landing` i18n key has deep nested structure (`landing.hero`, `landing.features`, `landing.pricing`, `landing.testimonials`, etc.) making the graph see false cross-feature connections. Real architectural bridges are obscured by i18n noise.  
**Fix:** Restructure i18n to feature-scoped namespaces: `landing.hero.*`, `dashboard.charts.*`, `auth.login.*`. This reduces graph noise and makes dependency tracking meaningful. Also improves maintainability — each feature owns its translations.

---

### [A-005] 🟢 994 Isolated Nodes Are Graph Noise, Not Code Issues
**Status:** **RESOLVED / NOT A CODE ISSUE**  
**Details:** This was a graph analysis artifact from 2026-05-30. The 1,156 nodes with degree ≤ 1 were mostly config properties (`$schema`, `version`, `budgets`), i18n leaf keys, AST method stubs, and test blocks. These inflate node count without representing architectural concepts.  
**Action:** No code fix needed. When querying the graph, filter by `file_type` or minimum degree (≥2) to reduce noise. For future extractions, consider collapsing JSON properties into their parent node and merging method stubs into their class node.

---

### [A-006] 🟡 Landing Page Eagerly Imports 6+ Sub-Components
**File:** `frontend/src/app/features/home/components/landing-new.component.ts`  
**Symptom:** `LandingNewComponent` directly imports `HeroSectionComponent`, `FeaturesGridComponent`, `PricingSectionComponent`, `RecommendationsCarouselComponent`, `ContactFormComponent`, and `AiSectionComponent`. All are loaded eagerly, even though only the hero and features are visible on initial render.  
**Fix:** Lazy-load below-the-fold sections (Pricing, Testimonials, Contact, AI) using Angular's `@defer` blocks or dynamic component loading. This improves initial load time and reduces the coupling surface.

---

## Deleted Files (MCP Removal - commit cbfbdb0)
> These files were removed during the MCP server removal refactor. Documented here for rollback reference.

**Backend:**
- `SuChefService/src/main/java/com/example/SuChefService/config/McpRateLimitFilter.java`
- `SuChefService/src/main/java/com/example/SuChefService/controller/McpController.java`
- `SuChefService/src/main/java/com/example/SuChefService/mcp/McpToolProvider.java`
- `SuChefService/src/main/java/com/example/SuChefService/mcp/McpToolProviderErrorHandlingTest.java`
- `SuChefService/src/main/java/com/example/SuChefService/mcp/McpToolProviderTest.java`

**Frontend:**
- `frontend/src/app/core/services/mcp.service.ts`

**Rollback tracker:** `docs/ai/mcp-rollback-tracker.md`