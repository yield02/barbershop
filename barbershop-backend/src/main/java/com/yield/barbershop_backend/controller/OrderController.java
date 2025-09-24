package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.order.OrderCreateDTO;
import com.yield.barbershop_backend.dto.order.OrderFilterDTO;
import com.yield.barbershop_backend.dto.order.OrderUpdateDTO;
import com.yield.barbershop_backend.dto.order.OrderUpdateStatusDTO;
import com.yield.barbershop_backend.model.AccountPrincipal;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;





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

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<Order>>> getOrderByFilter(OrderFilterDTO filter) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "", 
            new PagedResponse<>(orderService.getOrdersByFilter(filter))));
    }
    
    @PostMapping("")
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AccountPrincipal principal = (AccountPrincipal) authentication.getPrincipal();

        System.out.println("Principal: " + principal);

        if(authentication.getAuthorities().contains("ROLE_CUSTOMER")){
            System.out.println("Customer Create ID: " + principal.getId());
            orderCreateDTO.setCustomerId(principal.getId());
        }

        return ResponseEntity.created(null).body(new ApiResponse<Order>(true, "", orderService.createOrder(orderCreateDTO)));

    }
    
    /**
     * Update order status
     * 
     * @param orderId the order id
     * @param orderUpdateStatusDTO the order update status dto
     * @return the response entity
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(@PathVariable Long orderId, @RequestBody @Validated OrderUpdateStatusDTO orderUpdateStatusDTO) {
        orderService.updateOrderStatus(orderId, orderUpdateStatusDTO.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{orderId}")
    public ResponseEntity<ApiResponse<Void>> updateOrder(@PathVariable Long orderId, @RequestBody @Validated OrderUpdateDTO orderUpdateDTO) {
        orderService.updateOrder(orderId, orderUpdateDTO);
        return ResponseEntity.noContent().build();        
    }

}

