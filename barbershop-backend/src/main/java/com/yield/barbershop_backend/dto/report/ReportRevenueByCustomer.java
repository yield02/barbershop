package com.yield.barbershop_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class ReportRevenueByCustomer {
    
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Double totalNetRevenue;
    private TotalVisit totalVisit;

    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TotalVisit {
        protected Long appointments;
        protected Long orders;
    }

}
