package com.yield.barbershop_backend.dto.drink;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrinkDTO {

    @NotBlank(message = "drinkName is mandatory")
    private String drinkName;

    @NotBlank(message = "description is mandatory")
    private String description;

    @NotNull(message = "price is mandatory")
    private Double price;

    @NotNull(message = "stockQuantity is mandatory")
    private Integer stockQuantity;

    @NotBlank(message = "category is mandatory")
    private String category;

    @NotNull(message = "alcoholPercentage is mandatory")
    private Double alcoholPercentage;

    @NotBlank(message = "imageUrl is mandatory")
    private String imageUrl;

    private Boolean isActive = false;


}
