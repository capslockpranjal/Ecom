package org.example.zenvybackend.security.util;

import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.user.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @PostConstruct
    void validateConfiguration() {
        if (isPlaceholder(secret)) {
            throw new IllegalStateException("JWT_SECRET must be set to a strong base64-encoded secret.");
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            Keys.hmacShaKeyFor(keyBytes);
            int secretLength = keyBytes.length;
            log.info("JWT configuration validated successfully. JWT_SECRET loaded with {} bytes", secretLength);
            if (secretLength < 32) {
                log.warn("JWT_SECRET is only {} bytes; minimum recommended is 32 bytes for HMAC-SHA256", secretLength);
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT_SECRET must be a valid base64-encoded secret with enough bytes for HMAC signing.", ex);
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(String email, List<Role> roles) {

        List<String> authorities = roles.stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList());

        String token = Jwts.builder()
                .setSubject(email)
                .claim("roles", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey())
                .compact();
        
        log.debug("JWT token generated for email: {}, roles: {}, expiration: {} ms", email, authorities, accessExpiration);
        return token;
    }



    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }



    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }



    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims != null && claims.getExpiration().after(new Date())
                    && claims.getSubject() != null;
        } catch (JwtException ex) {
            log.debug("Token validation failed: {}", getJwtExceptionType(ex), ex);
            return false;
        }
    }

    private String getJwtExceptionType(Exception ex) {
        if (ex instanceof SignatureException) {
            return "SIGNATURE_EXCEPTION";
        } else if (ex instanceof MalformedJwtException) {
            return "MALFORMED_JWT_EXCEPTION";
        } else if (ex instanceof ExpiredJwtException) {
            return "EXPIRED_JWT_EXCEPTION";
        } else if (ex instanceof UnsupportedJwtException) {
            return "UNSUPPORTED_JWT_EXCEPTION";
        } else if (ex instanceof IllegalArgumentException) {
            return "ILLEGAL_ARGUMENT_EXCEPTION";
        } else {
            return "UNKNOWN_JWT_EXCEPTION (" + ex.getClass().getSimpleName() + ")";
        }
    }



    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            log.debug("Token is expired: {}", ex.getMessage());
            return ex.getClaims();
        } catch (SignatureException ex) {
            log.warn("JWT signature validation failed (possible JWT_SECRET mismatch): {}", ex.getMessage());
            throw ex;
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
            throw ex;
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
            throw ex;
        }
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Date extractIssuedAt(String token) {
        return extractAllClaims(token).getIssuedAt();
    }

    private boolean isPlaceholder(String value) {
        if (value == null) {
            return true;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty()
                || normalized.equals("change_me")
                || normalized.equals("your_secret")
                || normalized.equals("replace-with-a-strong-random-secret");
    }
}
