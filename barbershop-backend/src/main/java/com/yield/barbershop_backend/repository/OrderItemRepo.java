package com.yield.barbershop_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yield.barbershop_backend.model.OrderItem;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
    void deleteAllByOrderId(Long orderId);
} 
