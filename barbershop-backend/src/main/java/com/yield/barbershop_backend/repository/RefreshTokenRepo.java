package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.RefreshToken;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByUserId(Long userId);
    RefreshToken findByCustomerId(Long customerId);
    
    void deleteByUserId(Long userId);
    void deleteByCustomerId(Long customerId);
} 
