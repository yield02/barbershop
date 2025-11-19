package com.yield.barbershop_backend.dto.report;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarberRevenueDTO {
    
    private Long userId;
    private String fullName;
    private String role;
    private Double totalNetRevenue;
    private Integer totalAppointment;
    private Double percentageOfTotalNetRevenue;

}
