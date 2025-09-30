package com.yield.barbershop_backend.model;

import java.time.LocalDateTime;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private VerificationType type;
    private Boolean verified;
    private LocalDateTime verified_at;
    private LocalDateTime expiry_at;

    public enum VerificationType {
        EMAIL,
        PHONE
    }
}

