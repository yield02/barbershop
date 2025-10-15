package com.yield.barbershop_backend.dto.promotion;

import java.util.Date;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class PromotionCreateDTO {
    
    @NotBlank(message = "Promotion name is required")
    private String promotionName;
    @NotBlank(message = "Description is required")
    private String description;

    private Long maxApplicableQuantity = null;
    private Double discountPercentage = null;
    private Double discountAmount = null;

    @NotNull(message = "Start date is required")
    private Date startDate;

    @NotNull(message = "End date is required")
    private Date endDate;

    private Boolean isActive;

    @Valid
    private List<promotionItemCreateDTO> promotionItems;

    private Date createdAt = new Date(System.currentTimeMillis());
    private Date updatedAt = new Date(System.currentTimeMillis());

}
