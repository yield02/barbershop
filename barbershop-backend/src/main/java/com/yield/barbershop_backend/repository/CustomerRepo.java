package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> 
{

} 
