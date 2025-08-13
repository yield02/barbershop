package com.yield.barbershop_backend.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name="services")
@Data
public class Service {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;
    private String serviceName;
    private String description;
    private Double price;
    private Integer durationMinutes;
    private String category;
    private String imageUrl;
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;

}
