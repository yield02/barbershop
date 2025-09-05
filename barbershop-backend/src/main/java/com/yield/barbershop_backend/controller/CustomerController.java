package com.yield.barbershop_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.customer.CustomerFilterDTO;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/customers")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;


    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<Customer>>> getCustomersByFilter(CustomerFilterDTO filter) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "",
            new PagedResponse<>(customerService.getCustomersByFilter(filter))
        ));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Customer fetched successfully",
            customerService.getCustomerById(customerId)
        ));
    }
    
  

}
