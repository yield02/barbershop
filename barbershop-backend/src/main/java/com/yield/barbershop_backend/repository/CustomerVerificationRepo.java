package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yield.barbershop_backend.model.CustomerVerification;

public interface CustomerVerificationRepo extends JpaRepository<CustomerVerification, Long> {

    
} 
