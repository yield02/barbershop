package com.yield.barbershop_backend.dto;


import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import lombok.Data;

@Data
public class TokenDataEntity {

    private Long id;
    private Type type;
    private String email;
    private Collection<? extends GrantedAuthority> role; // admin, barber, customer

    public enum Type {
        CUSTOMER,
        STAFF
    }

}


