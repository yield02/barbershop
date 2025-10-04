package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.yield.barbershop_backend.model.Payment;

public interface PaymentRepo extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    
} 
