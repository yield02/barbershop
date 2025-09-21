package com.yield.barbershop_backend.dto.order;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderUpdateStatusDTO {
    
    @Pattern(regexp = "^(Pending|Processing|Completed|Cancelled)$", message = "Status must be one of the following values: Pending, Processing, Completed, Cancelled")
    private String status;

}
