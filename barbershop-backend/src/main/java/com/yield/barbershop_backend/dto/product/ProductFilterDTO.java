package com.yield.barbershop_backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDTO {

    private String productName;
    private Double maxPrice;
    private Double minPrice;
    private String category;
    private String brand;
    private Integer page = 0;
    private Integer pageSize = 10;

}
