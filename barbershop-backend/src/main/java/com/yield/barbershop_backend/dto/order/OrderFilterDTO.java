package com.yield.barbershop_backend.dto.order;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderFilterDTO {
    
    private Long userId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 0;
    private Integer pageSize = 10;

}
