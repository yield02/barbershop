package com.yield.barbershop_backend.specification;


import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.product.ProductFilterDTO;
import com.yield.barbershop_backend.model.Product;

import jakarta.persistence.criteria.Predicate;


public class ProductSpecification {

    public static Specification<Product> filters(ProductFilterDTO productFilter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<Predicate>();

            if (productFilter.getProductName() != null) {
                predicates.add(criteriaBuilder.like(root.get("productName"), "%" + productFilter.getProductName() + "%"));
            }
            if (productFilter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), productFilter.getMaxPrice()));
            }
            if (productFilter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), productFilter.getMinPrice()));
            }
            if (productFilter.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), productFilter.getCategory()));
            }
            if (productFilter.getBrand() != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), productFilter.getBrand()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
