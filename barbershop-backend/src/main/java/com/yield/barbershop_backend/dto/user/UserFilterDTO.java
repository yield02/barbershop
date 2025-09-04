package com.yield.barbershop_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {
    

    private String userName;
    private String role;
    private Boolean isActive;
    private Integer page = 0;
    private Integer pageSize = 10;

}
