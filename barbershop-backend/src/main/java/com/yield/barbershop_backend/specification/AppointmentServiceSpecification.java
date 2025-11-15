package com.yield.barbershop_backend.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class AppointmentServiceSpecification {
    

    public static Specification getAppointmentsByAppointmentIds(List<Long> appointmentIds) {
        return (root, query, cb) -> cb.in(root.get("appointmentId")).value(appointmentIds);
    }

}
