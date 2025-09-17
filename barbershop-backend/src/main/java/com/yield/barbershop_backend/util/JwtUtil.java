package com.yield.barbershop_backend.util;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.yield.barbershop_backend.dto.TokenDataEntity;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
            .claim("typeToken", tokenType.toString())
            .claim("type", tokenData.getType())
            .claim("role", tokenData.getRole())
            .claim("email", tokenData.getEmail())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMinutes * 1000))
            // .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000))
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
        }
        catch (ExpiredJwtException e) {
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public String extractTypeToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("typeToken", String.class);
        } catch (ClaimJwtException e) {
            return e.getClaims().get("typeToken", String.class);
        }
    }

    public Long extractSubject(String token) {
        try {
            return Long.parseLong(Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject());
        }
        catch (ClaimJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        }
    }

public Collection<? extends GrantedAuthority> extractRole(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .getBody();
    
    Object rolesClaim = claims.get("role");
    
    if (rolesClaim instanceof List) {
                List<Map<String, String>> roles = (List<Map<String, String>>) rolesClaim;
        return roles.stream()
                .map(roleMap -> new SimpleGrantedAuthority(roleMap.get("authority")))
                .collect(Collectors.toList());
    }
    return Collections.emptyList();
}

    public String extractType(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("type", String.class);
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("email", String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
            .setSigningKey(SECRET_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
        }
        catch (ExpiredJwtException e) {
            return true;
        }
        return false;
    }

    public void setAuthenticationCookies(String accessToken, String refreshToken, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // Set to true in production with HTTPS
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 15 minutes

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Set to true in production with HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }
}
