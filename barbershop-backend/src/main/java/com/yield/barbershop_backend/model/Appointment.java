package com.yield.barbershop_backend.model;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity(name = "appointments")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    private String customerName;
    private String customerPhone;
    private String customerEmail;


    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

      
    private String notes;
    private Double totalAmount;

    @Column(updatable = false)
    private Date createdAt;
    
    private Date updatedAt;

    @JsonIgnore
    @Column(name = "user_id")
    private Long userId;

    @JsonIgnore
    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @OneToMany(mappedBy = "appointment")
    @JsonManagedReference
    private List<AppointmentService> appointmentServices;

}


// CREATE TABLE IF NOT EXISTS `appointments` (
//   `appointment_id` int NOT NULL AUTO_INCREMENT,
//   `customer_name` varchar(100) NOT NULL,
//   `customer_phone` varchar(20) DEFAULT NULL,
//   `customer_email` varchar(100) DEFAULT NULL,
//   `customer_id` int DEFAULT NULL,
//   `user_id` int DEFAULT NULL,
//   `appointment_time` datetime NOT NULL,
//   `status` varchar(50) DEFAULT 'Pending',
//   `notes` text,
//   `total_amount` decimal(10,2) DEFAULT NULL,
//   `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
//   `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
//   PRIMARY KEY (`appointment_id`),
//   KEY `user_id` (`user_id`),
//   KEY `customer_id` (`customer_id`),
//   CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
//   CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
// ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;