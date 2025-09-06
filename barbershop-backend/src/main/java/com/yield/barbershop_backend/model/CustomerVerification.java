package com.yield.barbershop_backend.model;

import java.time.LocalDateTime;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "customer_verifications")
public class CustomerVerification {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String token_hash;
    private VerificationType type;
    private Boolean verified;
    private LocalDateTime verified_at;
    private LocalDateTime expiry_at;

}

