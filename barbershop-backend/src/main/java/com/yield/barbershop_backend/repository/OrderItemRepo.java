package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yield.barbershop_backend.model.OrderItem;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {


    
} 
