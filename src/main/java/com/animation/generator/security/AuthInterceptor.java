package com.animation.generator.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> ALLOWED_PATHS = List.of("/api/auth", "/api/auth/google-authorize");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        if (ALLOWED_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        String guestId = request.getHeader("guestId");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.validate(token);
                String userIdStr = claims.getSubject();
                Long userId = Long.parseLong(userIdStr);
                request.setAttribute("userId", userId);
                return true;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return false;
            }
        }
        if (guestId != null && guestId.startsWith("guest_")) {
            request.setAttribute("guestId", guestId);
            return true;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid credentials");
        return false;
    }
}
