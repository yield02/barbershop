package com.yield.barbershop_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.exception.DataConflictException;
import com.yield.barbershop_backend.model.OrderItem;
import com.yield.barbershop_backend.repository.OrderItemRepo;

@Service
public class OrderItemService {
    
    @Autowired
    OrderItemRepo orderItemRepo;

    @Transactional
    public List<OrderItem> createOrderItems(List<OrderItem> orderItems) {
        List <OrderItem> savedOrderItems = orderItemRepo.saveAll(orderItems);
        return savedOrderItems;
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepo.findByOrderId(orderId);
        return orderItems;
    }
}
