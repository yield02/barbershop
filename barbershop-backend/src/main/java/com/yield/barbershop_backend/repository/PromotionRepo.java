package com.yield.barbershop_backend.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.yield.barbershop_backend.model.Promotion;

public interface PromotionRepo extends 
JpaRepository<Promotion, Long>,
PagingAndSortingRepository<Promotion, Long>,
JpaSpecificationExecutor<Promotion>
{

    
    @Query("SELECT p FROM promotions p WHERE p.promotionId IN :promotionIds")
    List<Promotion> getActivePromotionsByIds(List<Long> promotionIds, Specification<Promotion> spec);
    

    @Modifying
    @Query("UPDATE promotions p SET p.maxApplicableQuantity = p.maxApplicableQuantity - 1 WHERE p.promotionId IN :promotionIds")
    void minusOneApplicableQuantityByPromotionIds(List<Long> promotionIds);

}
