package com.yield.barbershop_backend.dto.order;

import com.yield.barbershop_backend.model.Order.OrderStatus;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderUpdateStatusDTO {
    
    @Pattern(regexp = "^(Pending|Processing|Completed|Cancelled)$", message = "Status must be one of the following values: Pending, Processing, Completed, Cancelled")
    private OrderStatus status;

}
