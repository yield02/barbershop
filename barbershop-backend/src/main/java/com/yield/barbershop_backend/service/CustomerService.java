package com.yield.barbershop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.repository.CustomerRepo;

@Service
public class CustomerService {
    

    @Autowired
    private CustomerRepo customerRepo;

    public Customer getCustomerById(Long customerId) {
        return customerRepo.findById(customerId)
            .orElseThrow(() -> new DataNotFoundException("Customer not found"));
    }

}
