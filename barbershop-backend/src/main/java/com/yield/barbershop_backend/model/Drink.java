package com.yield.barbershop_backend.model;


import java.util.Date;

import com.yield.barbershop_backend.dto.drink.DrinkDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name="drinks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Drink {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drinkId;
    private String drinkName;
    private String description;
    private Double price;
    private Long stockQuantity;
    private String category;
    private Double alcoholPercentage;
    private String imageUrl;
    private Boolean isActive;

    @Column(updatable = false)
    private Date createdAt;
    private Date updatedAt;

    public Drink(DrinkDTO drink) {

        this.drinkName = drink.getDrinkName();
        this.description = drink.getDescription();
        this.price = drink.getPrice();
        this.stockQuantity = drink.getStockQuantity();
        this.category = drink.getCategory();
        this.alcoholPercentage = drink.getAlcoholPercentage();
        this.imageUrl = drink.getImageUrl();
        this.isActive = drink.getIsActive();
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
    
    }

    public Drink(Long drinkId, DrinkDTO drink) {
        this.drinkId = drinkId;
        this.drinkName = drink.getDrinkName();
        this.description = drink.getDescription();
        this.price = drink.getPrice();
        this.stockQuantity = drink.getStockQuantity();
        this.category = drink.getCategory();
        this.alcoholPercentage = drink.getAlcoholPercentage();
        this.imageUrl = drink.getImageUrl();
        this.isActive = drink.getIsActive();
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
    }

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