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
import org.springframework.web.bind.annotation.DeleteMapping;
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

        Boolean isAdmin = true;

        if(principal.getAuthorities().stream().anyMatch(role -> role.toString().equals("[ROLE_CUSTOMER]"))){
            System.out.println("Customer Create ID: " + principal.getId());
            orderCreateDTO.setCustomerId(principal.getId());
            orderCreateDTO.setUserId(null);
            isAdmin = false;
        }

        return ResponseEntity.created(null).body(new ApiResponse<Order>(true, "", orderService.createOrder(orderCreateDTO, isAdmin)));

    }
    
    /**
     * Update order status
     * 
     * @param orderId the order id
     * @param orderUpdateStatusDTO the order update status dto
     * @return the response entity
     */

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<Void>> updateOrderStatus(@PathVariable Long orderId, @RequestBody @Validated OrderUpdateStatusDTO orderUpdateStatusDTO) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AccountPrincipal accountPrincipal = (AccountPrincipal) authentication.getPrincipal();
        Long ownerId = accountPrincipal.getId();

        orderService.updateOrderStatus(orderId, orderUpdateStatusDTO.getStatus());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{orderId}")
    public ResponseEntity<ApiResponse<Void>> updateOrder(@PathVariable Long orderId, @RequestBody @Validated OrderUpdateDTO orderUpdateDTO) {
        orderService.updateOrder(orderId, orderUpdateDTO);
        return ResponseEntity.noContent().build();        
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long orderId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AccountPrincipal accountPrincipal = (AccountPrincipal) authentication.getPrincipal();
        Long ownerId = accountPrincipal.getId();

        Boolean isAdmin = authentication.getAuthorities().stream().anyMatch(role -> {
            return role.getAuthority().equals("ROLE_ADMIN") || role.getAuthority().equals("ROLE_MANAGER") || role.getAuthority().equals("ROLE_STAFF");
        });

        orderService.cancelOrder(orderId, ownerId, isAdmin);

        return ResponseEntity.noContent().build();
    }

}

