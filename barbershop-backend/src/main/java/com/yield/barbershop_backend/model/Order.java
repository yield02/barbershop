package com.yield.barbershop_backend.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Double totalAmount;
    private OrderStatus status;
    private String notes;


    @Column(updatable = false)
    private Date createdAt = new Date(System.currentTimeMillis());

    private Date updatedAt = new Date(System.currentTimeMillis());


    
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "order")
    @JsonManagedReference
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order")
    @JsonManagedReference
    private Payment payment;
    

    public enum OrderStatus {
        Pending, Processing, Completed, Cancelled
    }
}