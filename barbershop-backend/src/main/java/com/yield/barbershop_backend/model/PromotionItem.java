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

@Entity(name="promotionitems")
@Data
public class PromotionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionItemId;


    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "drink_id")
    private Long drinkId;


    @ManyToOne
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    @JsonBackReference
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    @JsonBackReference
    private Service service;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonBackReference
    private Product product;

    @ManyToOne
    @JoinColumn(name = "drink_id", insertable = false, updatable = false)
    @JsonBackReference
    private Drink drink;
}


// CREATE TABLE PromotionItems (
//     promotion_item_id INT AUTO_INCREMENT PRIMARY KEY,
//     promotion_id INT,
//     service_id INT,
//     product_id INT,
//     drink_id INT,
//     FOREIGN KEY (promotion_id) REFERENCES Promotions(promotion_id),
//     FOREIGN KEY (service_id) REFERENCES Services(service_id),
//     FOREIGN KEY (product_id) REFERENCES Products(product_id),
//     FOREIGN KEY (drink_id) REFERENCES Drinks(drink_id)
// );