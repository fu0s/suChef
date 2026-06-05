# MCP Server Rollback Tracker

## Goal
Remove the MCP server integration (EPIC-MCP-001) and replace with direct repository calls. The 38 `@Tool`-annotated methods in `McpToolProvider` were wired into `ChatClient` as `defaultTools()`, causing the local Ollama model (qwen2.5-coder:14b) to autonomously select tools — which was slow and unreliable.

## Approach
**Intent Classification + Direct Service Calls** will replace the tool-calling approach (separate phase after rollback).

## Rollback Phases

### Phase 1: Decouple Dependencies (Critical)

| Step | File | Change | Status |
|------|------|--------|--------|
| 1a | `SubscriptionService.java` | Remove `McpToolProvider` field, rewrite 6 methods to use repositories directly | ✅ |
| 1b | `DocumentService.java` | Remove `McpToolProvider` field, rewrite `getUserDocuments()` to use `documentRepository` | ✅ |
| 1c | `AiConfig.java` | Remove `McpToolProvider` from `chatClient()` bean | ✅ |
| 1d | `SecurityConfig.java` | Remove `McpRateLimitFilter` field, constructor param, and filter chain entry | ✅ |
| 1e | `ChatService.java` | Update system prompt to remove "MUST use the provided database tools" | ✅ |

### Phase 2: Delete MCP Files

| Step | File | Action | Status |
|------|------|--------|--------|
| 2a | `mcp/McpToolProvider.java` | Delete (1411 lines) | ✅ |
| 2b | `controller/McpController.java` | Delete | ✅ |
| 2c | `config/McpRateLimitFilter.java` | Delete | ✅ |

### Phase 3: Config Cleanup

| Step | File | Change | Status |
|------|------|--------|--------|
| 3a | `pom.xml` | Remove `spring-ai-starter-mcp-server-webflux` and `spring-ai-starter-mcp-client` | ✅ |
| 3b | `application.properties` | Remove MCP config lines (47-54, 62-63) | ✅ |
| 3c | `application-prod.properties` | Remove MCP config lines (13-15) | ✅ |

### Phase 4: Frontend Cleanup

| Step | File | Change | Status |
|------|------|--------|--------|
| 4a | `suchef/src/app/core/services/mcp.service.ts` | Delete | ✅ |
| 4b | `frontend/src/app/core/services/mcp.service.ts` | Delete | ✅ |
| 4c | `documents.service.ts` | Rewrite to call `/api/documents` directly | ✅ |
| 4d | `subscription.service.ts` | Rewrite to call `/api/subscription/*` directly | ✅ |

### Phase 5: Test Updates

| Step | File | Change | Status |
|------|------|--------|--------|
| 5a | `McpToolProviderTest.java` | Delete (48 tests) | ✅ |
| 5b | `McpToolProviderErrorHandlingTest.java` | Delete (15 tests) | ✅ |
| 5c | `SubscriptionServiceTest.java` | Rewrite mocks (remove McpToolProvider, add repository mocks) | ✅ |
| 5d | `DocumentServiceTest.java` | Rewrite `getUserDocuments` test mock | ✅ |

### Validation

| Check | Status |
|-------|--------|
| `mvn compile` passes | ✅ |
| `mvn test` passes (36/36) | ✅ |
| No `McpToolProvider` references remain in backend | ✅ |
| No MCP config in properties | ✅ |
| No `McpService` references in frontend | ✅ |

## Files Changed Log

### 2026-06-04 — Full Rollback Complete

**Backend — Edited (5 files):**
- `SubscriptionService.java` — removed McpToolProvider dependency, inlined subscription logic using repositories directly
- `DocumentService.java` — removed McpToolProvider dependency, query documentRepository directly
- `AiConfig.java` — removed defaultTools from ChatClient bean
- `SecurityConfig.java` — removed McpRateLimitFilter from constructor and filter chain
- `ChatService.java` — updated system prompt to remove tool-calling instructions

**Backend — Deleted (5 files):**
- `mcp/McpToolProvider.java` (1411 lines)
- `controller/McpController.java`
- `config/McpRateLimitFilter.java`
- `mcp/McpToolProviderTest.java` (48 tests)
- `mcp/McpToolProviderErrorHandlingTest.java` (15 tests)

**Config — Edited (3 files):**
- `pom.xml` — removed 2 MCP dependencies
- `application.properties` — removed MCP config lines
- `application-prod.properties` — removed MCP config lines

**Frontend — Deleted (2 files):**
- `suchef/src/app/core/services/mcp.service.ts`
- `frontend/src/app/core/services/mcp.service.ts`

**Frontend — Rewritten (4 files):**
- `suchef/src/app/core/services/documents.service.ts` — calls `/api/documents` directly
- `suchef/src/app/core/services/subscription.service.ts` — calls `/api/subscription/*` directly
- `frontend/src/app/core/services/documents.service.ts` — same
- `frontend/src/app/core/services/subscription.service.ts` — same

**Tests — Rewritten (2 files):**
- `SubscriptionServiceTest.java` — 10 tests, all repository-based
- `DocumentServiceTest.java` — 8 tests, getUserDocuments uses documentRepository

## Next Steps
- **Phase 2 (separate task):** Implement IntentClassifier + QueryRouter for natural language DB access in chat
- **Cleanup:** Remove `docs/ai/mcp-tool-catalog.md` (no longer relevant)
