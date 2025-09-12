package com.yield.barbershop_backend.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.order.OrderFilterDTO;

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
}
