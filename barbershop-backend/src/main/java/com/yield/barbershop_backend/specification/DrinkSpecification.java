package com.yield.barbershop_backend.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.DrinkFilterDTO;
import com.yield.barbershop_backend.model.Drink;

import jakarta.persistence.criteria.Predicate;

public class DrinkSpecification {
    
    public static Specification<Drink> filters(DrinkFilterDTO drinkFilter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (drinkFilter.getDrinkName() != null) {
                predicates.add(criteriaBuilder.like(root.get("drinkName"), "%" + drinkFilter.getDrinkName() + "%"));
            }
            if (drinkFilter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), drinkFilter.getMaxPrice()));
            }
            if (drinkFilter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), drinkFilter.getMinPrice()));
            }
            if (drinkFilter.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), drinkFilter.getCategory()));
            }
            if (drinkFilter.getBrand() != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), drinkFilter.getBrand()));
            }
            if (drinkFilter.getMaxAlcoholPercentage() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("alcoholPercentage"), drinkFilter.getMaxAlcoholPercentage()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
