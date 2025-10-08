package com.yield.barbershop_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "orderitems")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;
    private Long quantity;
    private Double originalPrice; // price before discount
    private Double finalPrice; // price after discount
    private Double discountAmount; // discount amount
    
    private String name;
    
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "drink_id")
    private Long drinkId;

    @Column(name = "order_id")
    private Long orderId;



    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Product.class)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonBackReference
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Drink.class)
    @JoinColumn(name = "drink_id", insertable = false, updatable = false)
    @JsonBackReference
    private Drink drink;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Order.class)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @JsonBackReference
    private Order order;

    
}