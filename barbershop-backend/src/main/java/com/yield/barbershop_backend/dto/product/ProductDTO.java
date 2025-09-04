package com.yield.barbershop_backend.dto.product;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductDTO {

    @NotBlank(message = "productName is mandatory")
    private String productName;

    @NotBlank(message = "description is mandatory")
    private String description;

    @NotNull(message = "price is mandatory")
    private Double price;

    @NotNull(message = "stockQuantity is mandatory")
    private Integer stockQuantity;

    @NotBlank(message = "category is mandatory")
    private String category;

    @NotBlank(message = "brand is mandatory")
    private String brand;

    @NotBlank(message = "imageUrl is mandatory")
    private String imageUrl;

    private Boolean isActive = false;

}
