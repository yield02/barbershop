package com.yield.barbershop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.dto.AppointmentFilterDTO;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Appointment;
import com.yield.barbershop_backend.repository.AppointmentRepo;
import com.yield.barbershop_backend.specification.AppointmentSpecification;

@Service
public class AppointmentService {
    

    @Autowired
    private AppointmentRepo appointmentRepo;

    public Page<Appointment> getAppointmentsWithFilter(AppointmentFilterDTO filters) {

        Pageable page = PageRequest.of(filters.getPage(), filters.getPageSize());

        return appointmentRepo.findAll(AppointmentSpecification.filters(filters), page);
    }

    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Appointment not found"));
    }

    public void deleteAppointment(Long appointmentId) {
        appointmentRepo.deleteById(appointmentId);
    }

}
