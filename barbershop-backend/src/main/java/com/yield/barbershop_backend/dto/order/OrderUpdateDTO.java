package com.yield.barbershop_backend.dto.order;

import java.util.ArrayList;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderUpdateDTO {

    @NotBlank(message = "Notes are required")
    private String notes;

    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private ArrayList<OrderProductItemCreateDTO> products;
    private ArrayList<OrderProductItemCreateDTO> drinks;

}
