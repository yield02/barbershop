package com.yield.barbershop_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.model.Appointment;
import java.util.List;


@Repository
public interface AppointmentRepo extends 
JpaRepository<Appointment, Long>,
JpaSpecificationExecutor<Appointment>,
PagingAndSortingRepository<Appointment, Long>
{

    Optional<Appointment> findByAppointmentIdAndCustomerId(Long appointmentId, Long customerId);
}
