package com.yield.barbershop_backend.dto.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Notes are required")
    private String notes;

    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;


    private ArrayList<OrderProductItemCreateDTO> products;
    private ArrayList<OrderProductItemCreateDTO> drinks;


    @Nullable
    private Date createdAt = new Date(System.currentTimeMillis());
    @Nullable
    private Date updatedAt = new Date(System.currentTimeMillis());
}
