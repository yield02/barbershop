package com.yield.barbershop_backend.dto.promotion;

import java.util.Date;

import lombok.Data;

@Data
public class PromotionFilterDTO {
    

    private String promotionName;
    private Double maxDiscountPercentage;
    private Double maxDiscountAmount;
    
    private Date startDate;
    private Date endDate;
    
    private Boolean isActive;
    
    private Integer page = 0;
    private Integer pageSize = 10;
}
