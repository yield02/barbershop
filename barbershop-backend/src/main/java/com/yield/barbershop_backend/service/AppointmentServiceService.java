package com.yield.barbershop_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.model.AppointmentService;
import com.yield.barbershop_backend.repository.AppointmentServiceRepo;
import com.yield.barbershop_backend.specification.AppointmentServiceSpecification;

@Service
public class AppointmentServiceService {
    
    @Autowired
    private AppointmentServiceRepo appointmentServiceRepo;


    // public List<com.yield.barbershop_backend.model.AppointmentService> getAllAppointments() {
    //     return appointmentServiceRepo.saveAll(null)
    // }

    public List<AppointmentService> getAllAppointmentsByAppointmentIds(List<Long> appointmentIds) {
        return appointmentServiceRepo.findAll(AppointmentServiceSpecification.getAppointmentsByAppointmentIds(appointmentIds));
    }

    public void deleteAppointmentServicesByAppointmentId(Long appointmentId) {
        appointmentServiceRepo.deleteAllByAppointmentId(appointmentId);
    }


    public List<AppointmentService> createAppointmentServices(List<AppointmentService> appointmentServices) {
        return appointmentServiceRepo.saveAll(appointmentServices);
    }
}
