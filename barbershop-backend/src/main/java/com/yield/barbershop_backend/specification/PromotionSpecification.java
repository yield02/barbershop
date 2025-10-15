package com.yield.barbershop_backend.specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.promotion.PromotionFilterDTO;
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

    public static Specification<Promotion> getPromotionsByFilter(PromotionFilterDTO filter) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(filter.getPromotionName() != null) {
                predicates.add(cb.like(root.get("promotionName"), "%" + filter.getPromotionName() + "%"));
            }

            if(filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.getStartDate()));
            }

            if(filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), filter.getEndDate()));
            }

            if(filter.getMaxDiscountPercentage() != null) {
                predicates.add(cb.lessThan(root.get("discountPercentage"), filter.getMaxDiscountPercentage()));
            }

            if(filter.getMaxDiscountAmount() != null) {
                predicates.add(cb.lessThan(root.get("discountAmount"), filter.getMaxDiscountAmount()));
            }
            
            if(filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
