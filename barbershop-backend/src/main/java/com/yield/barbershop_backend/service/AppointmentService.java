package com.yield.barbershop_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.AppointmentFilterDTO;
import com.yield.barbershop_backend.dto.CreateAppointmentDTO;
import com.yield.barbershop_backend.dto.UpdateAppointmentDTO;
import com.yield.barbershop_backend.exception.DataConflictException;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Appointment;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.repository.AppointmentRepo;
import com.yield.barbershop_backend.specification.AppointmentSpecification;


@Service
public class  AppointmentService {
    

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private AppointmentServiceService appointmentServiceService;

    // @Autowired
    // private AppointmentServiceRepo appointmentServiceRepo;

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

    @Transactional
    public Appointment createAppointment(CreateAppointmentDTO appointment) throws DataNotFoundException, DataConflictException {
        // Check and get user
        User user = userService.getUserById(appointment.getUserId());
        
        // Check services
        List<com.yield.barbershop_backend.model.Service> services = serviceService.findExistedServiceIds(appointment.getServiceIds());
        if (!services.isEmpty() && (services.size() != appointment.getServiceIds().size())) {

            List<Long> nonExistedIds = appointment.getServiceIds().stream()
                .filter(id -> !services.stream().map(com.yield.barbershop_backend.model.Service::getServiceId).collect(Collectors.toSet()).contains(id))
                .toList();

            throw new DataNotFoundException("Services not found with ids: " + nonExistedIds);
        }

        // Caculate endTime
        LocalDateTime endTime = appointment.getStartTime().plusMinutes(
            services.stream()
                .map(com.yield.barbershop_backend.model.Service::getDurationMinutes)
                .reduce(0, Integer::sum)
        );

        //Caculate totalAmount
        Double totalAmount = services.stream()
            .map(com.yield.barbershop_backend.model.Service::getPrice)
            .reduce(0.0, Double::sum);



        // Get CustomerId By Id, Se bo sung chuc nang nay khi da them jwt
        Long customerId = 1L;

        Customer customer = customerService.getCustomerById(customerId);

        // Check for appointment conflicts
        checkAppointmentConflict(appointment.getStartTime(), endTime, user.getUserId());

        // Create Appointment
        Appointment newAppointment = new Appointment();
 
        newAppointment.setCustomerName(customer.getFullName());
        newAppointment.setCustomerPhone(customer.getPhoneNumber());
        newAppointment.setCustomerEmail(customer.getEmail());
        newAppointment.setCustomerId(customer.getCustomerId());

        newAppointment.setUserId(user.getUserId());

        newAppointment.setTotalAmount(totalAmount);
        newAppointment.setStartTime(appointment.getStartTime());
        newAppointment.setEndTime(endTime);
        newAppointment.setStatus("Pending");
        newAppointment.setNotes(appointment.getNotes());
        newAppointment.setCreatedAt(appointment.getCreatedAt());
        newAppointment.setUpdatedAt(appointment.getUpdatedAt());
        Appointment savedAppointment = appointmentRepo.save(newAppointment);


        // add appointmentservices
        List<com.yield.barbershop_backend.model.AppointmentService> appointmentServices = services.stream().map(service -> {
            com.yield.barbershop_backend.model.AppointmentService appointmentService = new com.yield.barbershop_backend.model.AppointmentService();
            appointmentService.setAppointmentId(savedAppointment.getAppointmentId());
            appointmentService.setServiceId(service.getServiceId());
            appointmentService.setPrice(service.getPrice());
            return appointmentService;
        }).collect(Collectors.toList());

        appointmentServiceService.createAppointmentServices(appointmentServices);

        return savedAppointment;
    }


    @Transactional
    public Appointment updateAppointment(Long appointmentId, UpdateAppointmentDTO appointment) {

        //Check appointment exsited

        Appointment existingAppointment = appointmentRepo.findById(appointmentId)
            .orElseThrow(() -> new DataNotFoundException("Appointment not found with id: " + appointmentId));

        // Check status appointment

        if (existingAppointment.getStatus().equals("Cancelled") || existingAppointment.getStatus().equals("Completed")) {
            throw new DataConflictException("Cannot update appointment with status: " + existingAppointment.getStatus());
        }

        // Check and get user
        User user = userService.getUserById(appointment.getUserId());
        
        // Check services
        List<com.yield.barbershop_backend.model.Service> services = serviceService.findExistedServiceIds(appointment.getServiceIds());
        if (!services.isEmpty() && (services.size() != appointment.getServiceIds().size())) {

            List<Long> nonExistedIds = appointment.getServiceIds().stream()
                .filter(id -> !services.stream().map(com.yield.barbershop_backend.model.Service::getServiceId).collect(Collectors.toSet()).contains(id))
                .toList();

            throw new DataNotFoundException("Services not found with ids: " + nonExistedIds);
        }

        // Caculate endTime
        LocalDateTime endTime = appointment.getStartTime().plusMinutes(
            services.stream()
                .map(com.yield.barbershop_backend.model.Service::getDurationMinutes)
                .reduce(0, Integer::sum)
        );

        //Caculate totalAmount
        Double totalAmount = services.stream()
            .map(com.yield.barbershop_backend.model.Service::getPrice)
            .reduce(0.0, Double::sum);



        // Get CustomerId By Id, Se bo sung chuc nang nay khi da them jwt
        Long customerId = 1L;

        Customer customer = customerService.getCustomerById(customerId);

        // Check for appointment conflicts
        checkAppointmentConflictExceptCurrent(appointment.getStartTime(), endTime, user.getUserId(), appointmentId);


        // update Appointment

        existingAppointment.setUserId(user.getUserId());
        existingAppointment.setTotalAmount(totalAmount);
        existingAppointment.setStartTime(appointment.getStartTime());
        existingAppointment.setEndTime(endTime);
        existingAppointment.setNotes(appointment.getNotes());
        existingAppointment.setCreatedAt(appointment.getCreatedAt());
        existingAppointment.setUpdatedAt(appointment.getUpdatedAt());
        Appointment savedAppointment = appointmentRepo.save(existingAppointment);

        // delete old services

        appointmentServiceService.deleteAppointmentServicesByAppointmentId(appointmentId);

        // add new appointmentservices
        List<com.yield.barbershop_backend.model.AppointmentService> appointmentServices = services.stream().map(service -> {
            com.yield.barbershop_backend.model.AppointmentService appointmentService = new com.yield.barbershop_backend.model.AppointmentService();
            appointmentService.setAppointmentId(savedAppointment.getAppointmentId());
            appointmentService.setServiceId(service.getServiceId());
            appointmentService.setPrice(service.getPrice());
            return appointmentService;
        }).collect(Collectors.toList());

        appointmentServiceService.createAppointmentServices(appointmentServices);

        return savedAppointment;
    }


    public List<Appointment> checkAppointmentsConflict(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        return appointmentRepo.findAll(AppointmentSpecification.checkAppointmentsConflict(startTime, endTime, userId));
    }

    public void checkAppointmentConflict(LocalDateTime startTime, LocalDateTime endTime, Long userId) {
        List<Appointment> conflictingAppointments = checkAppointmentsConflict(startTime, endTime, userId);
        if(!conflictingAppointments.isEmpty()) {
            List<Long> conflictIds = conflictingAppointments.stream().map(Appointment::getAppointmentId).collect(Collectors.toList());
            throw new DataConflictException("Appointment time conflicts with " + conflictIds, conflictIds);
        }
    }

    public void checkAppointmentConflictExceptCurrent(LocalDateTime startTime, LocalDateTime endTime, Long userId, Long currentAppointmentId) {
        List<Appointment> conflictingAppointments = checkAppointmentsConflict(startTime, endTime, userId);

        conflictingAppointments.removeIf(appointment -> appointment.getAppointmentId().equals(currentAppointmentId));

        if(!conflictingAppointments.isEmpty()) {
            List<Long> conflictIds = conflictingAppointments.stream().map(Appointment::getAppointmentId).collect(Collectors.toList());
            throw new DataConflictException("Appointment time conflicts with " + conflictIds, conflictIds);
        }
    }

}
