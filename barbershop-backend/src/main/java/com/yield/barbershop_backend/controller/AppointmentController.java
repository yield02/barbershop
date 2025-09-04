package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.appointment.AppointmentFilterDTO;
import com.yield.barbershop_backend.dto.appointment.CreateAppointmentDTO;
import com.yield.barbershop_backend.dto.appointment.UpdateAppointmentDTO;
import com.yield.barbershop_backend.dto.appointment.UpdatePaymentConfirmedAppointmentDTO;
import com.yield.barbershop_backend.dto.appointment.UpdateStatusAppointmentDTO;
import com.yield.barbershop_backend.model.Appointment;
import com.yield.barbershop_backend.service.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("")
    public ResponseEntity<PagedResponse<Appointment>> getAppointmentsWithFilter(AppointmentFilterDTO filters) {
        return ResponseEntity.ok(new PagedResponse<>(appointmentService.getAppointmentsWithFilter(filters)));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<Appointment>> getAppointmentById(@PathVariable Long appointmentId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "",
            appointment
        ));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Appointment>> createAppointment(@RequestBody CreateAppointmentDTO appointment) {
        Appointment createdAppointment = appointmentService.createAppointment(appointment);
        return ResponseEntity.created(null).body(new ApiResponse<>(
            true,
            "Appointment created successfully",
            createdAppointment
        ));
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<Appointment>> updateAppointment(@PathVariable Long appointmentId, @RequestBody UpdateAppointmentDTO appointment) {
        Appointment updatedAppointment = appointmentService.updateAppointment(appointmentId, appointment);
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Appointment updated successfully",
            updatedAppointment
        ));
    }

    @PatchMapping("/{appointmentId}/payment-status")
    public ResponseEntity<ApiResponse<Void>> updatePaymentConfirmedStatus(@PathVariable Long appointmentId, @RequestBody @Validated UpdatePaymentConfirmedAppointmentDTO payment) {
        appointmentService.updatePaymentConfirmedStatus(appointmentId, payment);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{appointmentId}/status/cancelled")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(@PathVariable Long appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }


}