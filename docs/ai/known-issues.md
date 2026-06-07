# Known Issues & Edge Cases
> Owner: `qa-reviewer` | Last updated: 2026-06-07  
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

### [A-002] 🟡 DocumentsService Backend Coupling to SubscriptionService
**Files:** `SuChefService/src/main/java/com/example/SuChefService/service/DocumentService.java`  
**Symptom:** Backend `DocumentService` depends on `SubscriptionService` for document size limits and usage tracking (lines 41, 97, 124). Frontend `DocumentsService` is used by Dashboard and Documents upload only (Chat no longer uses it post-MCP removal). A change to document handling ripples to subscription logic.  
**Fix:** Decouple document upload validation from subscription checks. Use a `DocumentUploadValidator` interface that can be implemented differently per subscription tier, or move limit checks to a separate service layer.

---

## Resolved / PR Open

| Issue | PR Branch | Status |
|-------|-----------|--------|
| [A-001] AuthService God Node | `fix/auth-god-node-a001` | PR open |
| [A-003] ChatService-Subscription coupling | `fix/chat-subscription-decouple-a003` | PR open |
| [A-004] i18n flat structure | `fix/i18n-restructure-a004` | PR open |
| [A-005] Isolated graph nodes | — | **RESOLVED** (analysis artifact) |
| [A-006] Landing eager imports | `fix/landing-lazy-load-a006` | PR open |

---

## Deleted Files (MCP Removal - commit cbfbdb0)
> These files were removed during the MCP server removal refactor. Documented here for rollback reference.

**Backend:**
- `SuChefService/src/main/java/com/example/SuChefService/config/McpRateLimitFilter.java`
- `SuChefService/src/main/java/com/example/SuChefService/controller/McpController.java`
- `SuChefService/src/main/java/com/example/SuChefService/mcp/McpToolProvider.java`
- `SuChefService/src/test/java/com/example/SuChefService/mcp/McpToolProviderErrorHandlingTest.java`
- `SuChefService/src/test/java/com/example/SuChefService/mcp/McpToolProviderTest.java`

**Frontend:**
- `frontend/src/app/core/services/mcp.service.ts`

**Rollback tracker:** `docs/ai/mcp-rollback-tracker.md`