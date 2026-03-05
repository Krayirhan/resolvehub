package com.resolvehub.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resolvehub.auth.service.RequestRateLimiter;
import com.resolvehub.common.api.ApiErrorResponse;
import com.resolvehub.common.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RequestRateLimiter requestRateLimiter;
    private final ObjectMapper objectMapper;
    private final int requestLimitPerMinute;

    public RateLimitFilter(
            RequestRateLimiter requestRateLimiter,
            ObjectMapper objectMapper,
            @Value("${resolvehub.rate-limit.requests-per-minute:120}") int requestLimitPerMinute
    ) {
        this.requestRateLimiter = requestRateLimiter;
        this.objectMapper = objectMapper;
        this.requestLimitPerMinute = requestLimitPerMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = resolveRateLimitKey(request);
        if (!requestRateLimiter.allow(key, requestLimitPerMinute)) {
            response.setStatus(429);
            response.setContentType("application/json");
            ApiErrorResponse body = new ApiErrorResponse(
                    "RATE_LIMITED",
                    "Rate limit exceeded",
                    Instant.now(),
                    List.of()
            );
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return "user:" + user.id();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
