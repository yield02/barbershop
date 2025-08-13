package com.yield.barbershop_backend.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name="drinks")
@Data
public class Drink {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drinkId;
    private String drinkName;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private String category;
    private Double alcoholPercentage;
    private String imageUrl;
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;

}

// CREATE TABLE IF NOT EXISTS `drinks` (
//   `drink_id` int NOT NULL AUTO_INCREMENT,
//   `drink_name` varchar(100) NOT NULL,
//   `description` text,
//   `price` decimal(10,2) NOT NULL,
//   `stock_quantity` int DEFAULT '0',
//   `category` varchar(50) DEFAULT NULL,
//   `alcohol_percentage` decimal(5,2) DEFAULT NULL,
//   `image_url` varchar(255) DEFAULT NULL,
//   `is_active` tinyint(1) DEFAULT '1',
//   `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
//   `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
//   PRIMARY KEY (`drink_id`)
// ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;