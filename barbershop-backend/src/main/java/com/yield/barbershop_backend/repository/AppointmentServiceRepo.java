package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.AppointmentService;

@Repository
public interface AppointmentServiceRepo extends 
JpaRepository<AppointmentService, Long>
 {

} 
