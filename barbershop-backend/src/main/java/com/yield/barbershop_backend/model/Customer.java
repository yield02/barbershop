package com.yield.barbershop_backend.model;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "customers")
@Data
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @JsonIgnore
    private String password;

    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String notes;
    
    @Column(updatable = false)
    private Date createdAt = new Date(System.currentTimeMillis());
    private Date updatedAt = new Date(System.currentTimeMillis());


}
