package com.yield.barbershop_backend.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.yield.barbershop_backend.dto.appointment.AppointmentFilterDTO;
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

            if (filter.getStartTime() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), filter.getStartTime()));
            }

            if (filter.getEndTime() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime"), filter.getEndTime()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Appointment> checkAppointmentsConflict(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        return (root, query, criteriaBuilder) -> {
            Predicate timeConflict = criteriaBuilder.and(
                criteriaBuilder.greaterThan(root.get("endTime"), startTime),
                criteriaBuilder.lessThan(root.get("startTime"), endTime),
                criteriaBuilder.equal(root.get("userId"), userId),
                criteriaBuilder.notEqual(root.get("status"), "Cancelled")
            );
            return criteriaBuilder.and(timeConflict);
        };
    }

    public static Specification<Appointment> getCompletedAppointmentsBetweenTwoDates(Date startDate, Date endDate) {
        return (root, query, criteriaBuilder) -> {

            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), "Completed"));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
