package com.yield.barbershop_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    Optional findById(Long id);
    
} 
