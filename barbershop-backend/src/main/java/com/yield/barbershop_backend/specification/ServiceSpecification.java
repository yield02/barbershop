package com.yield.barbershop_backend.specification;


import java.util.ArrayList;
import java.util.List;


import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.ServiceFilterDTO;
import com.yield.barbershop_backend.model.Service;

import jakarta.persistence.criteria.Predicate;

public class ServiceSpecification {

    public static Specification<Service> filter(ServiceFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();


            if (filter.getServiceName() != null) {
                predicates.add(criteriaBuilder.like(root.get("serviceName"), "%" + filter.getServiceName() + "%"));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            if (filter.getCategory() != null) {
                predicates.add(criteriaBuilder.like(root.get("category"), "%" + filter.getCategory() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
