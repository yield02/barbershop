package com.yield.barbershop_backend.model;

import java.sql.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity(name="promotions")
@Data
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;
    private String promotionName;
    private String description;
    private Long maxApplicableQuantity;
    private Double discountPercentage;
    private Double discountAmount;
    private Date startDate;
    private Date endDate;
    private Boolean isActive;

    @Column(updatable = false)
    private Date createdAt;
    private Date updatedAt;


    @OneToMany(mappedBy = "promotion")
    @JsonManagedReference
    private List<PromotionItem> promotionItems; // List of items associated with the promotion
}

// CREATE TABLE Promotions (
//     promotion_id INT AUTO_INCREMENT PRIMARY KEY,
//     promotion_name VARCHAR(100) NOT NULL,
//     description TEXT,
//     discount_percentage DECIMAL(5, 2), -- Phần trăm giảm giá
//     discount_amount DECIMAL(10, 2), -- Số tiền giảm giá cố định (nếu có)
//     start_date DATE,
//     end_date DATE,
//     is_active BOOLEAN DEFAULT TRUE,
//     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
// );
