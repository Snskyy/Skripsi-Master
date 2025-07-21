package com.inventory.inventory.security;

import com.inventory.inventory.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static io.jsonwebtoken.Claims.EXPIRATION;

@Component
public class JwtUtil {
//    private final String SECRET = "secretkey";
//    private static final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);  // Automatically secure key

    @Value("${jwt.secret}")
    private String secretKeyString;

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())

                .claim("role", user.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 2)) // 2 hari
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
//    Date expiryDate = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 100); // 100 years
//    Date expiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10); // 10 hours

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey())  // Use the secure key to parse
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, User user) {
        final String email = extractUsername(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey()) // Secure key
                .parseClaimsJws(token)
                .getBody();
    }

}
