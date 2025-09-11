package com.yield.barbershop_backend.config.security.filters;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.yield.barbershop_backend.dto.TokenDataEntity;
import com.yield.barbershop_backend.model.RefreshToken;
import com.yield.barbershop_backend.service.RefreshTokenService;
import com.yield.barbershop_backend.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtFilter extends OncePerRequestFilter {
    

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;




/*************  ✨ Windsurf Command ⭐  *************/
    /**
     * This method is used to filter every incoming request. It checks if the request has valid access token and refresh token.
     * If the access token is valid and not expired, it sets the authentication context and allows the request to pass through.
     * If the access token is expired, it checks if the refresh token is valid and not expired. If it is, it resigns the access token and refresh token
     * and saves the new refresh token to the database. If the refresh token is also expired, it throws an exception.
     * If there is an exception during the process, it is caught and handled by the exception handler.
     * @param request the incoming request
     * @param response the response to the request
     * @param filterChain the filter chain to continue the filtering process
     * @throws ServletException if there is an error during the filtering process
     * @throws IOException if there is an error during the filtering process
     */
/*******  2c7c12de-82ff-4bf0-97da-f16d9ea64ce2  *******/
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    
        try {

        String accessToken = request.getHeader("access_token");
        String refreshToken = request.getHeader("refresh_token");

        if(accessToken == null || refreshToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check access token is valid
        if(!jwtUtil.validateToken(accessToken) || !jwtUtil.extractTypeToken(accessToken).equals(JwtUtil.TokenType.ACCESS.toString())) {
            throw new MalformedJwtException("Access Token is invalid");
        }


        // Check access token is not expired
        if(!jwtUtil.isTokenExpired(accessToken)) {
            setAuthenticationContext(accessToken, request);
            filterChain.doFilter(request, response);
            return;
        }

        // Check refresh token is valid
        
        if(!jwtUtil.validateToken(refreshToken) || !jwtUtil.extractTypeToken(refreshToken).equals(JwtUtil.TokenType.REFRESH.toString())) {
            throw new MalformedJwtException("Access Token is expired and Refresh Token is invalid");
        }

        // Check refresh token is expired
        if(jwtUtil.isTokenExpired(refreshToken)) {
            throw new ExpiredJwtException(null, null, "Access Token and Refresh token is expired");
        }
        
        
        String typeAccount = jwtUtil.extractType(refreshToken);
        
        // 
        if(typeAccount.equals(TokenDataEntity.Type.CUSTOMER.toString())) {
            handleRefreshTokenCustomer(refreshToken, request, response);
        }
        else if(typeAccount.equals(TokenDataEntity.Type.STAFF.toString())) {
            handleRefreshTokenStaff(refreshToken, request, response);
        }
        }
        catch(Exception e) {
            System.out.println(e.getMessage()  );
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    public void handleRefreshTokenStaff(String refreshToken, HttpServletRequest request, HttpServletResponse response) {

        Long userId = jwtUtil.extractSubject(refreshToken);
        RefreshToken refreshTokenDb = refreshTokenService.getTokenByUserId(userId);
        
        // Token is not equal Token in database
        if(!refreshToken.equals(refreshTokenDb.getToken())) {
            refreshTokenService.deleteByUserId(userId);
            throw new JwtException("Refresh Token is incorrect");
        }

        // Resign AccessToken, RefreshToken and Save RefreshToken to Database

        TokenDataEntity tokenData = new TokenDataEntity();
        tokenData.setId(userId);
        tokenData.setType(TokenDataEntity.Type.STAFF);
        tokenData.setEmail(jwtUtil.extractEmail(refreshToken));
        tokenData.setRole(jwtUtil.extractRole(refreshToken));

        String newAccessToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.ACCESS);
        String newRefreshToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.REFRESH);

        jwtUtil.setAuthenticationCookies(newAccessToken, newRefreshToken, response);

        refreshTokenService.saveUserToken(userId, newRefreshToken);

        setAuthenticationContext(newAccessToken, request);
    }

    public void handleRefreshTokenCustomer(String refreshToken, HttpServletRequest request, HttpServletResponse response) {

        Long customerId = jwtUtil.extractSubject(refreshToken);

        RefreshToken refreshTokenDb = refreshTokenService.getTokenByCustomerId(customerId);

        // Token is not equal Token in database
        if(!refreshToken.equals(refreshTokenDb.getToken()) || refreshTokenDb == null) {
            refreshTokenService.deleteByCustomerId(customerId);
            throw new JwtException("Refresh Token is incorrect");
        }

        // Resign AccessToken, RefreshToken and Save RefreshToken To Database;

        TokenDataEntity tokenData = new TokenDataEntity();
        tokenData.setId(customerId);
        tokenData.setType(TokenDataEntity.Type.CUSTOMER);
        tokenData.setEmail(jwtUtil.extractEmail(refreshToken));
        tokenData.setRole(jwtUtil.extractRole(refreshToken));


        String newAccessToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.ACCESS);
        String newRefreshToken = jwtUtil.generateToken(tokenData, JwtUtil.TokenType.REFRESH);

        jwtUtil.setAuthenticationCookies(newAccessToken, newRefreshToken, response);        
        
        refreshTokenService.saveCustomerToken(customerId, newRefreshToken);

        setAuthenticationContext(newAccessToken, request);
    }


    public void setAuthenticationContext(String token, HttpServletRequest request) {

        String email = jwtUtil.extractEmail(token);
        Collection<? extends GrantedAuthority> role = jwtUtil.extractRole(token);


        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, role);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
