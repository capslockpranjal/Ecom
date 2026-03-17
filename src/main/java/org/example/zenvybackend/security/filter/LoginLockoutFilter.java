package org.example.zenvybackend.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.util.CachedBodyHttpServletRequest;
import org.example.zenvybackend.security.handler.CustomAuthenticationEntryPoint;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom Spring Security filter that checks if the account is locked before allowing login.
 * Runs for POST /auth/login and returns 403 if the user account is locked (and lock hasn't expired).
 */
@Component
@RequiredArgsConstructor
public class LoginLockoutFilter extends OncePerRequestFilter {

    private static final long LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().equals("/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        String email = extractEmail(wrappedRequest.getCachedBody());
        if (email != null && !email.isBlank()) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && Boolean.TRUE.equals(user.getIsLocked())) {
                if (user.getLockTime() != null
                        && user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
                    // Lock expired - let the request through; AuthService will unlock
                } else {
                    authenticationEntryPoint.commence(request, response,
                            new BadCredentialsException("Account locked. Try again after 30 minutes"));
                    return;
                }
            }
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private String extractEmail(byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            return node.has("email") ? node.get("email").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
