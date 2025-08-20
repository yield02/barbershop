package com.yield.barbershop_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrinkFilterDTO {

    private String drinkName;
    private Double minPrice;
    private Double maxPrice;
    private String category;
    private Double maxAlcoholPercentage;
    private String brand;
    private Byte page = 0;
    private Byte pageSize = 10;

}
