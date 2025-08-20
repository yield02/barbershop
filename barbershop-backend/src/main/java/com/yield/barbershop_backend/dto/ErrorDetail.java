package com.yield.barbershop_backend.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    
    private String code;         // example: "USER_NOT_FOUND"
    private String description; 
    private List<?> details;


}
