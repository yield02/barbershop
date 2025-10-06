package com.yield.barbershop_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Order;
import java.util.List;


@Repository
public interface OrderRepo extends 
JpaRepository<Order, Long>,
PagingAndSortingRepository<Order, Long>,
JpaSpecificationExecutor<Order>

{

    Optional<Order> findById(Long id);
    
    @Modifying
    @Query("UPDATE orders o SET o.status = ?2 WHERE o.id = ?1")
    int updateOrderStatusByOrderId(Long orderId, String status);
    
    @EntityGraph(attributePaths = {"orderItems", "payment"})
    List<Order> findAll(Specification<Order> spec);
} 
