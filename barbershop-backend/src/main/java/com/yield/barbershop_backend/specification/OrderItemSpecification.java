package com.yield.barbershop_backend.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.model.OrderItem;

public class OrderItemSpecification {
    

    public static Specification<OrderItem> getOrderItemsByOrderIds(List<Long> orderIds) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.in(root.get("orderId")).value(orderIds);
        };

    }

}
