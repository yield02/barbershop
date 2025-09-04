package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.model.Appointment;

@Repository
public interface AppointmentRepo extends 
JpaRepository<Appointment, Long>,
JpaSpecificationExecutor<Appointment>,
PagingAndSortingRepository<Appointment, Long>
{

    @Modifying
    @Transactional
    @Query("UPDATE appointments a SET a.paymentConfirmed = :paymentConfirmed WHERE a.appointmentId = :appointmentId")
    int updatePaymentConfirmedStatus(Long appointmentId, Boolean paymentConfirmed);

}
