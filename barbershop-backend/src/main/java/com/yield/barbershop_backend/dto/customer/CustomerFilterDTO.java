package com.yield.barbershop_backend.dto.customer;

import lombok.Data;

@Data
public class CustomerFilterDTO {
    
    private String fullName;
    private String phoneNumber;
    private String email;
    private Integer page = 0;
    private Integer pageSize = 10;

}
