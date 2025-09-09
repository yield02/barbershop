package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Customer;
import java.util.List;
import java.util.Optional;


@Repository
public interface CustomerRepo extends 
JpaRepository<Customer, Long>,
PagingAndSortingRepository<Customer, Long>,
JpaSpecificationExecutor<Customer>
{
    @Query("SELECT c FROM customers c WHERE c.email = :email OR c.phoneNumber = :phoneNumber")
    List<Customer> findByEmailOrPhoneNumber(String email, String phoneNumber);

    Optional<Customer> findByEmail(String email);

} 
