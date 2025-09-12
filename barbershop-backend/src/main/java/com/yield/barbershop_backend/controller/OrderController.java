package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.order.OrderFilterDTO;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/orders")
public class OrderController {
    

    @Autowired
    private OrderService orderService;


    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(
            new ApiResponse<>(true, "", orderService.getOrderById(orderId))
        );
    }


    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<Order>>> getOrderByFilter(OrderFilterDTO filter) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "", 
            new PagedResponse<>(orderService.getOrdersByFilter(filter))));
    }
}
