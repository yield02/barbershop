package com.yield.barbershop_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity(name = "appointmentservices")
@Data
public class AppointmentService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentServiceId;
    private Double price;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "service_id")
    private Long serviceId;

    @ManyToOne
    @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
    @JsonBackReference
    private Appointment appointment;

    @ManyToOne   
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private Service service;

}

// CREATE TABLE IF NOT EXISTS `appointmentservices` (
//   `appointment_service_id` int NOT NULL AUTO_INCREMENT,
//   `appointment_id` int DEFAULT NULL,
//   `service_id` int DEFAULT NULL,
//   `price` decimal(10,2) NOT NULL,
//   PRIMARY KEY (`appointment_service_id`),
//   KEY `appointment_id` (`appointment_id`),
//   KEY `service_id` (`service_id`),
//   CONSTRAINT `appointmentservices_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`),
//   CONSTRAINT `appointmentservices_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`service_id`)
// ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

