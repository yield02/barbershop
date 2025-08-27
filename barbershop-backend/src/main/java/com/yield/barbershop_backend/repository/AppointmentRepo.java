package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Appointment;

@Repository
public interface AppointmentRepo extends 
JpaRepository<Appointment, Long>,
JpaSpecificationExecutor<Appointment>,
PagingAndSortingRepository<Appointment, Long>
{

}
