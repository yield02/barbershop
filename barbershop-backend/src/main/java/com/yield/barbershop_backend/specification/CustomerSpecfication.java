package com.yield.barbershop_backend.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.customer.CustomerFilterDTO;
import com.yield.barbershop_backend.model.Customer;

import jakarta.persistence.criteria.Predicate;

public class CustomerSpecfication {

    public static Specification<Customer> filters(CustomerFilterDTO filter)  {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getFullName() != null) {
                predicates.add(criteriaBuilder.like(root.get("fullName"), "%" + filter.getFullName() + "%"));
            }
            if (filter.getPhoneNumber() != null) {
                predicates.add(criteriaBuilder.like(root.get("phoneNumber"), "%" + filter.getPhoneNumber() + "%"));
            }
            if (filter.getEmail() != null) {
                predicates.add(criteriaBuilder.like(root.get("email"), "%" + filter.getEmail() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
