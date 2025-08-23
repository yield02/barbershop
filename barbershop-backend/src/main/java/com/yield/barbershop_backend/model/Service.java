package com.yield.barbershop_backend.model;

import java.sql.Date;

import com.yield.barbershop_backend.dto.ServiceDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name="services")
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(updatable = false)
    private Date createdAt;
    private Date updatedAt;


    public Service(ServiceDTO service) {
        this.serviceName = service.getServiceName();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.durationMinutes = service.getDurationMinutes();
        this.category = service.getCategory();
        this.imageUrl = service.getImageUrl();
        this.isActive = service.getIsActive();
        this.createdAt = service.getCreatedAt();
        this.updatedAt = service.getUpdatedAt();
    }

    public Service(Long serviceId, ServiceDTO service) {
        this.serviceId = serviceId;
        this.serviceName = service.getServiceName();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.durationMinutes = service.getDurationMinutes();
        this.category = service.getCategory();
        this.imageUrl = service.getImageUrl();
        this.isActive = service.getIsActive();
        this.createdAt = service.getCreatedAt();
        this.updatedAt = service.getUpdatedAt();
    }


}
