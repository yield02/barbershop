package com.yield.barbershop_backend.util;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

import com.yield.barbershop_backend.dto.TokenDataEntity;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Builder.Default;

@Component
public class JwtUtil {

    private final Key SECRET_KEY = Keys.hmacShaKeyFor("your-256-bit-secret-your-256-bit-secret".getBytes());

    @Value("${app.yield.barbershop.jwt.access-token.expiration}")
    private Long ACCESSTOKEN_MINUTES_EXPIRATION;
    @Value("${app.yield.barbershop.jwt.refresh-token.expiration}")
    private Long REFRESHTOKEN_DAYS_EXPIRATION;

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    public Long getAccessTokenExpirationMinutes() {
        return this.ACCESSTOKEN_MINUTES_EXPIRATION;
    }

    public Long getRefreshTokenExpirationDays() {
        return this.REFRESHTOKEN_DAYS_EXPIRATION;
    }

    public String generateToken(TokenDataEntity tokenData, TokenType tokenType) {

        Long expirationMinutes = tokenType == TokenType.ACCESS ? ACCESSTOKEN_MINUTES_EXPIRATION*60 : REFRESHTOKEN_DAYS_EXPIRATION*60*60*24;

        return Jwts.builder()
            .setSubject(tokenData.getId().toString())
            .claim("type", tokenData.getType())
            .claim("role", tokenData.getRole())
            .claim("email", tokenData.getEmail())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMinutes * 1000))
            .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
            .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractSubject(String token) {
        String subject = Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
        return Long.parseLong(subject);
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("role", String.class);
    }

    public String extractType(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("type", String.class);
    }

    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
        return expiration.before(new Date());
    }
}
