package com.yield.barbershop_backend.dto.order;

import lombok.Data;

@Data
public class OrderProductItemCreateDTO {
    
    private Long itemId;
    private Long quantity;

}
