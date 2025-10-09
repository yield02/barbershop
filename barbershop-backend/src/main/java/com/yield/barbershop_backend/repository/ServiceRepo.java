package com.yield.barbershop_backend.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Service;

@Repository
public interface ServiceRepo extends 
JpaRepository<Service, Long>, 
   PagingAndSortingRepository<Service, Long>, 
   JpaSpecificationExecutor<Service> 
{
   @EntityGraph(attributePaths = {"promotionItems"})
   @Query("SELECT s FROM services s WHERE s.serviceId IN :serviceIds AND s.isActive = true")
   List<Service> findExistedIds(List<Long> serviceIds);

}