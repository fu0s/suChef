package com.example.SuChefService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * F-4: Per-user rate limiter for MCP tool calls.
 * Sliding window: 60 requests per 60 seconds per user.
 * Returns HTTP 429 when exceeded.
 */
@Component
public class McpRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_MS = 60_000;

    private final ConcurrentHashMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/mcp/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = resolveUserId();
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        SlidingWindow window = windows.computeIfAbsent(userId, k -> new SlidingWindow());
        if (!window.tryAcquire()) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"RATE_LIMITED\",\"message\":\"Too many MCP requests. Limit: "
                + MAX_REQUESTS + " per minute.\",\"retryAfterSeconds\":60}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        return principal instanceof org.springframework.security.core.userdetails.UserDetails
                ? ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername()
                : principal.toString();
    }

    /** Cleanup stale windows every 5 minutes via lazy expiry. */
    public int getActiveUserCount() {
        windows.entrySet().removeIf(e -> e.getValue().isExpired());
        return windows.size();
    }

    private static class SlidingWindow {
        private volatile long windowStart = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger(0);

        boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                synchronized (this) {
                    if (now - windowStart > WINDOW_MS) {
                        windowStart = now;
                        count.set(0);
                    }
                }
            }
            return count.incrementAndGet() <= MAX_REQUESTS;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > WINDOW_MS * 2;
        }
    }
}
