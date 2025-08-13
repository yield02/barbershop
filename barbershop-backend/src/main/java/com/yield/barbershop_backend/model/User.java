package com.yield.barbershop_backend.model;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    
    @JsonIgnore
    private String password;

    private String fullName;
    private String role;
    private String email;
    private String phoneNumber;
    private Byte isActive;
    private Date createdAt;
    private Date updatedAt;

}

