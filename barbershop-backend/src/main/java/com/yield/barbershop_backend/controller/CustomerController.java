package com.yield.barbershop_backend.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.customer.CustomerFilterDTO;
import com.yield.barbershop_backend.dto.customer.CustomerRegisterDTO;
import com.yield.barbershop_backend.dto.customer.CustomerUpdateDTO;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



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

    @PostMapping("")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody @Validated CustomerRegisterDTO customer) {
        Customer createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.created(null).body(new ApiResponse<>(
            true,
            "Customer created successfully",
            createdCustomer
        ));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> updateCustomer(@PathVariable Long customerId, @RequestBody @Validated CustomerUpdateDTO customer) {
        
        // Check customerId = authenticated customer id

        
        customerService.updateCustomer(customerId, customer);
        return ResponseEntity.noContent().build();
    }

}
