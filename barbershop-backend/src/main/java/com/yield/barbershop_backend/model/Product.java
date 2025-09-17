package com.yield.barbershop_backend.model;

import java.sql.Date;

import com.yield.barbershop_backend.dto.product.ProductDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name="products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String productName;
    private String description;
    private Double price;
    private Long stockQuantity;
    private String category;
    private String brand;
    private String imageUrl;

    private Boolean isActive;

    @Column(updatable = false)
    private Date createdAt;
    private Date updatedAt;


    public Product(ProductDTO product) {
        this.productName = product.getProductName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.category = product.getCategory();
        this.brand = product.getBrand();
        this.imageUrl = product.getImageUrl();
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
        this.isActive = product.getIsActive();
    }

    public Product(Long productId, ProductDTO product) {
        this.productId = productId;
        this.productName = product.getProductName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.category = product.getCategory();
        this.brand = product.getBrand();
        this.imageUrl = product.getImageUrl();
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
        this.isActive = product.getIsActive();
    }

}
