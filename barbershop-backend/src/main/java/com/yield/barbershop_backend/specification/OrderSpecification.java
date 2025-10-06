package com.yield.barbershop_backend.specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.order.OrderFilterDTO;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.model.OrderItem;
import com.yield.barbershop_backend.model.Order.OrderStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class OrderSpecification {
    public static Specification getOrderWithFilter(OrderFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if(filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), filter.getUserId()));

            }

            if(filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));

            }

            if(filter.getStartTime() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), filter.getStartTime()));

            }

            if(filter.getEndTime() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), filter.getEndTime()));                
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Order> getOrderDateBetween(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);  
        };
    }

      public static Specification<Order> getOrderSuccessAndDateBetween(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            predicates.add(criteriaBuilder.equal(root.get("status"), OrderStatus.Completed));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Order> getOrderDrinkAndDateBetween(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            predicates.add(criteriaBuilder.equal(root.get("status"), OrderStatus.Completed));

            Join<Order, OrderItem> orderItems = root.join("orderItems", JoinType.INNER);

            predicates.add(criteriaBuilder.isNotNull(orderItems.get("drinkId")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));  
        };
    }

    public static Specification<Order> getOrderProductAndDateBetween(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));


            Join<Order, OrderItem> orderItems = root.join("orderItems", JoinType.INNER);

            predicates.add(criteriaBuilder.isNotNull(orderItems.get("productId")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));  
        };
    }
}
