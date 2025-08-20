package com.yield.barbershop_backend.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemStockQuantityDTO {
    
    @Min(value = 0, message = "Stock quantity must be a non-negative integer")
    private Integer stockQuantity;

}
