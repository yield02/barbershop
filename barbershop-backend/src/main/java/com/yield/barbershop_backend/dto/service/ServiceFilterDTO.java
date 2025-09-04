package com.yield.barbershop_backend.dto.service;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFilterDTO {

    private String serviceName;
    private Double minPrice;
    private Double maxPrice;
    private String category;
    private Integer page = 0;
    private Integer pageSize = 10;

}
