package com.yield.barbershop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.dto.customer.CustomerFilterDTO;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.repository.CustomerRepo;
import com.yield.barbershop_backend.specification.CustomerSpecfication;

@Service
public class CustomerService {
    

    @Autowired
    private CustomerRepo customerRepo;

    public Customer getCustomerById(Long customerId) {
        return customerRepo.findById(customerId)
            .orElseThrow(() -> new DataNotFoundException("Customer not found"));
    }

    public Page<Customer> getCustomersByFilter(CustomerFilterDTO filter) {
        Pageable pageable = PageRequest.ofSize(filter.getPageSize()).withPage(filter.getPage());
        return customerRepo.findAll(CustomerSpecfication.filters(filter), pageable);
    }

}
