package com.yield.barbershop_backend.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.AppointmentFilterDTO;
import com.yield.barbershop_backend.model.Appointment;

import jakarta.persistence.criteria.Predicate;

public class AppointmentSpecification {

    public static Specification<Appointment> filters(AppointmentFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), filter.getUserId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getAppointmentTime() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("appointmentTime"), filter.getAppointmentTime()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
