package com.yield.barbershop_backend.dto.service;

import java.sql.Date;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {

    private String serviceName;
    private String description;
    private Double price;
    private Integer durationMinutes;
    private String category;
    private String imageUrl;
    private Boolean isActive = false;

    private Date createdAt = new Date(System.currentTimeMillis());
    private Date updatedAt = new Date(System.currentTimeMillis());

}