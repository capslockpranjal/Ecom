package org.example.zenvybackend.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.security.handler.CustomAuthenticationEntryPoint;
import org.example.zenvybackend.security.service.BlacklistCacheService;
import org.example.zenvybackend.security.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final BlacklistCacheService blacklistCacheService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;





    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/auth/logout")){
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

            if(blacklistCacheService.isBlacklisted(token)){
                authenticationEntryPoint.commence(request, response,
                        new BadCredentialsException("Token is blacklisted"));
                return;
            }

            if(!jwtUtil.validateToken(token)){
                authenticationEntryPoint.commence(request, response,
                        new BadCredentialsException("Token invalid or expired"));
                return;
            }

            String email = jwtUtil.extractEmail(token);

            if(SecurityContextHolder.getContext().getAuthentication() == null){

                List<SimpleGrantedAuthority> authorities =
                        jwtUtil.extractRoles(token)
                                .stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Invalid JWT token"));
            return;
        }

        filterChain.doFilter(request, response);
    }
}