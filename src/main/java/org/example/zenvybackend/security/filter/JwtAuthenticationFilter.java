package org.example.zenvybackend.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.security.handler.CustomAuthenticationEntryPoint;
import org.example.zenvybackend.security.service.BlacklistCacheService;
import org.example.zenvybackend.security.service.CustomUserDetails;
import org.example.zenvybackend.security.util.JwtUtil;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final BlacklistCacheService blacklistCacheService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
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
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("Invalid JWT token"));
            return;
        }

        filterChain.doFilter(request, response);
    }
}