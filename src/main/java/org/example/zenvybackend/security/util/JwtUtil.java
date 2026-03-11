package org.example.zenvybackend.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.zenvybackend.user.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /* -------------------- GENERATE TOKEN -------------------- */

    public String generateToken(String email, List<Role> roles) {

        List<String> authorities = roles.stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /* -------------------- EXTRACT EMAIL -------------------- */

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /* -------------------- EXTRACT ROLES -------------------- */

    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    /* -------------------- VALIDATE TOKEN -------------------- */

    public boolean validateToken(String token) {

        Claims claims = extractAllClaims(token);

        return claims.getExpiration().after(new Date())
                && claims.getSubject() != null;
    }

    /* -------------------- PARSE CLAIMS -------------------- */

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        }
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Date extractIssuedAt(String token) {
        return extractAllClaims(token).getIssuedAt();
    }
}