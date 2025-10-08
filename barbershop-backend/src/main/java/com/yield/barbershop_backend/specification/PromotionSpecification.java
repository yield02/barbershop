package com.yield.barbershop_backend.specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.model.Promotion;

import jakarta.persistence.criteria.Predicate;

public class PromotionSpecification {
    
    public static Specification<Promotion> getActivePromotions() {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            
            Date date = new Date();
            predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), date));
            predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), date));
            predicates.add(cb.greaterThan(root.get("maxApplicableQuantity"), 0));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
