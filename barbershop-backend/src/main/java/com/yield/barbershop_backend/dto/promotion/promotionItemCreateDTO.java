package com.yield.barbershop_backend.dto.promotion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class promotionItemCreateDTO {
    
    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotBlank(message = "Item type is required")
    @Pattern(regexp = "^(SERVICE|PRODUCT|DRINK)$", message = "Item type must be one of the following values: SERVICE, PRODUCT, DRINK")
    private PromotionItemType itemType;

    public enum PromotionItemType{
        SERVICE,
        PRODUCT,
        DRINK
    }
}
