package com.yield.barbershop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.repository.OrderRepo;


@Service
public class OrderService {


    @Autowired
    private OrderRepo orderRepo;
    
    public Order getOrderById(Long id) {
        return orderRepo
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
    }
    
}
