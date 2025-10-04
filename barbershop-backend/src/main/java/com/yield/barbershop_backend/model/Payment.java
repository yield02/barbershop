package com.yield.barbershop_backend.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long paymentId;
    Date paymentDate;
    Double amount;
    String paymentMethod;
    String transactionId;

    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    String notes;

    @Column(name = "customer_id")
    Long customerId;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "appointment_id")
    Long appointmentId;

    @Column(name = "order_id")
    Long orderId;
    
    @ManyToOne(targetEntity = Customer.class)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    Customer customer;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    User user;

    @OneToOne(targetEntity = Appointment.class)
    @JsonBackReference
    @JoinColumn(name = "appointment_id", insertable = false, updatable = false)
    Appointment appointment;

    @OneToOne(targetEntity = Order.class)
    @JsonBackReference
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    Order order;



    public enum PaymentStatus {
        Successful,
        Failed,
        Refunded,
        Pending
    }

}


// CREATE TABLE IF NOT EXISTS `payments` (
//   `payment_id` int NOT NULL AUTO_INCREMENT,
//   `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- Thời gian thanh toán
//   `amount` decimal(10,2) NOT NULL, -- Số tiền của giao dịch thanh toán này
//   `payment_method` varchar(50) NOT NULL, -- Ví dụ: 'Cash', 'Card', 'Momo', 'Bank Transfer', 'Voucher'
//   `transaction_id` varchar(100) DEFAULT NULL, -- ID giao dịch từ cổng thanh toán (nếu có)
//   `status` varchar(50) DEFAULT 'Successful', -- Trạng thái của giao dịch thanh toán: 'Successful', 'Failed', 'Refunded', 'Pending'
//   `notes` text, -- Ghi chú về giao dịch
//   `customer_id` int DEFAULT NULL,
//   `user_id` int DEFAULT NULL, -- Nhân viên thực hiện thanh toán
//   `appointment_id` int DEFAULT NULL,
//   `order_id` int DEFAULT NULL,
//   PRIMARY KEY (`payment_id`),
//   KEY `customer_id` (`customer_id`),
//   KEY `user_id` (`user_id`),
//   KEY `appointment_id` (`appointment_id`),
//   KEY `order_id` (`order_id`),
//   CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE SET NULL,
//   CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
//   CONSTRAINT `payments_ibfk_3` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`appointment_id`) ON DELETE SET NULL,
//   CONSTRAINT `payments_ibfk_4` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE SET NULL
// ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
