package com.yield.barbershop_backend.dto.report;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CategoryRevenueDTO {
    private String category;
    private Double totalRevenue;
    private Double netRevenue;
    private Long totalTransactions;
    private Double percentageOfTotalNetRevenue;
}
