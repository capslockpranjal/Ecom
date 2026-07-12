package org.example.zenvybackend.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.security.handler.CustomAuthenticationEntryPoint;
import org.example.zenvybackend.security.service.BlacklistCacheService;
import org.example.zenvybackend.security.service.CustomUserDetails;
import org.example.zenvybackend.security.util.JwtUtil;
import org.example.zenvybackend.user.repository.TokenRepository;
import org.example.zenvybackend.user.token.TokenType;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final BlacklistCacheService blacklistCacheService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;




    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/auth")){
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            boolean blacklistedInCache = blacklistCacheService.isBlacklisted(token);
            boolean blacklistedInDb = false;

            if (!blacklistedInCache) {
                blacklistedInDb = tokenRepository.existsByTokenAndTypeAndExpiryDateAfter(
                        token,
                        TokenType.BLACKLISTED,
                        LocalDateTime.now()
                );

                if (blacklistedInDb) {
                    blacklistCacheService.blacklistToken(token);
                }
            }

            if(blacklistedInCache || blacklistedInDb){
                authenticationEntryPoint.commence(request, response,
                        new BadCredentialsException("Token is blacklisted"));
                return;
            }

            if(!jwtUtil.validateToken(token)){
                log.warn("Token validation returned false for email extraction - likely expired or signature mismatch");
                authenticationEntryPoint.commence(request, response,
                        new BadCredentialsException("Token invalid or expired"));
                return;
            }

            String email;
            try {
                email = jwtUtil.extractEmail(token);
                log.debug("JWT token validated successfully for email: {}", email);
            } catch (Exception ex) {
                String exceptionType = getExceptionType(ex);
                log.error("Failed to extract email from token ({}): {}", exceptionType, ex.getMessage(), ex);
                throw ex;
            }
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            Date issuedAt = jwtUtil.extractIssuedAt(token);

            if(user.getPasswordUpdateDate() != null &&
                    issuedAt.toInstant().isBefore(
                            user.getPasswordUpdateDate()
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                    )){

                authenticationEntryPoint.commence(request, response,
                        new BadCredentialsException("Token expired due to password change"));
                return;
            }

            if(SecurityContextHolder.getContext().getAuthentication() == null){

                List<SimpleGrantedAuthority> authorities =
                        jwtUtil.extractRoles(token)
                                .stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                CustomUserDetails userDetails = new CustomUserDetails(
                        user.getId(),
                        user.getEmail(),
                        authorities
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            String exceptionType = getExceptionType(ex);
            String errorMessage = String.format("JWT validation failed (%s): %s", exceptionType, ex.getMessage());
            log.error(errorMessage, ex);
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException(errorMessage));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getExceptionType(Exception ex) {
        String className = ex.getClass().getSimpleName();
        if (className.equals("SignatureException")) {
            return "SIGNATURE_MISMATCH";
        } else if (className.equals("MalformedJwtException")) {
            return "MALFORMED_TOKEN";
        } else if (className.equals("ExpiredJwtException")) {
            return "TOKEN_EXPIRED";
        } else if (className.equals("UnsupportedJwtException")) {
            return "UNSUPPORTED_TOKEN";
        } else if (className.equals("IllegalArgumentException")) {
            return "INVALID_FORMAT";
        }
        return className;
    }
}
