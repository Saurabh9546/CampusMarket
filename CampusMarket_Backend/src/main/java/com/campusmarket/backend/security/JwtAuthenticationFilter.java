package com.campusmarket.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Runs once per request. If a valid, verified-account JWT is present in the
 * Authorization header, sets the authenticated principal so downstream
 * controllers/services can read it via SecurityContextHolder.
 *
 * If no Bearer token is present, this filter does nothing to the security
 * context and simply passes the request along — it never rejects a request
 * itself. Whether that request is then allowed through is decided entirely
 * by SecurityConfig's authorizeHttpRequests rules (e.g. /api/v1/auth/**
 * is permitAll(), so it passes with no authentication; any other path
 * requires an authenticated principal, which won't exist if no token was
 * supplied, and Spring Security's own entry point returns 401/403 for those).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {
            String userId = jwtService.extractUserId(token);

            // Unverified accounts are blocked at the filter level, before
            // any controller or service logic runs.
            if (!jwtService.isVerified(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNVERIFIED_ACCOUNT");
                return;
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList() // single STUDENT role for V1 — no authorities needed yet
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}