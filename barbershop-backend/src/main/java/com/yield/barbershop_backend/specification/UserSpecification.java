package com.yield.barbershop_backend.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.user.UserFilterDTO;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {
    

    public static Specification filter(UserFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getFullName() != null) {
                predicates.add(criteriaBuilder.like(root.get("fullName"), "%" + filter.getFullName() + "%"));
            }
            if (filter.getRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), filter.getRole()));
            }
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), filter.getIsActive()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


}
