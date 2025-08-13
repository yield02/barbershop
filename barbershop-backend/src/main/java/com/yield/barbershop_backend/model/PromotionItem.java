package com.yield.barbershop_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity(name="promotion_items")
@Data
public class PromotionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionItemId;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    @JsonBackReference
    private Promotion promotion;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "drink_id")
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