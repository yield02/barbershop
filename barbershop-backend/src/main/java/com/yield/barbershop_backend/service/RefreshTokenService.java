package com.yield.barbershop_backend.service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.model.RefreshToken;
import com.yield.barbershop_backend.repository.RefreshTokenRepo;
import com.yield.barbershop_backend.util.JwtUtil;


@Service
public class RefreshTokenService {
    
    @Autowired
    private RefreshTokenRepo refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public RefreshToken getTokenByCustomerId(Long customerId) {
        return refreshTokenRepository.findByCustomerId(customerId);
    }

    public RefreshToken getTokenByUserId(Long userId) {
        return refreshTokenRepository.findByUserId(userId);
    }

    public void saveUserToken(Long userId, String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId);
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setUserId(userId);
            
        }
        
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(jwtUtil.getRefreshTokenExpirationDays()));
        refreshToken.setToken(token);
        refreshTokenRepository.save(refreshToken);
    }

    public void saveCustomerToken(Long customerId, String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByCustomerId(customerId);
        if(refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setCustomerId(customerId);
        }
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(jwtUtil.getRefreshTokenExpirationDays()));
        refreshToken.setToken(token);
        refreshTokenRepository.save(refreshToken);

    }


    @Transactional
    public void deleteByCustomerId(Long customerId) {
        refreshTokenRepository.deleteByCustomerId(customerId);
    }
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

}
