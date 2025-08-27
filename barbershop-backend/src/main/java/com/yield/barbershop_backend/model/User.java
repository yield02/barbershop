package com.yield.barbershop_backend.model;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
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

    @Column(updatable = false)
    private Date createdAt;

    
    private Date updatedAt;


    @JsonIgnore
    public String getMaskedEmail() {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        String firstChar = email.substring(0, 2);

        String stars = "";

        for(int i = 0; i <= email.substring(2, atIndex-1).length(); i++) {
            stars += "*";
        }

        String masked = firstChar + stars + email.substring(atIndex);
        return masked;
    }

    @JsonIgnore
    public String getMaskedPhoneNumber() {
        String masked = phoneNumber.substring(0, phoneNumber.length() - 4);
        for (int i = 0; i < masked.length(); i++) {
            masked = masked.replace(masked.charAt(i), '*');
        }
        masked += phoneNumber.substring(phoneNumber.length() - 4);
        return masked;
    }
 
}

