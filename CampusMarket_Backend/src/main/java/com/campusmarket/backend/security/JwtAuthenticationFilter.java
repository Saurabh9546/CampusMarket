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
 * Runs once per request. Reads the Authorization header, and if a valid JWT
 * is present, sets the authenticated principal so downstream controllers/
 * services can read it via SecurityContextHolder. Requests to /api/v1/auth/**
 * never reach the "reject if missing/invalid" logic in a meaningful way
 * because SecurityConfig already permitAll()s them — this filter still runs
 * on those paths, but simply finds no token and moves on, which is fine.
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
            filterChain.doFilter(request, response); // no token — let SecurityConfig's authorization rules decide
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {
            String userId = jwtService.extractUserId(token);

            // Enforce the "unverified accounts are blocked" rule from the
            // integration spec at the filter level, not per-controller.
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