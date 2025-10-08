package com.yield.barbershop_backend.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.yield.barbershop_backend.model.Promotion;

public interface PromotionRepo extends 
PagingAndSortingRepository<Promotion, Long>,
JpaSpecificationExecutor<Promotion>
{

    
    @Query("SELECT p FROM promotions p WHERE p.promotionId IN :promotionIds")
    Map<Long, Promotion> getActivePromotionsByIds(List<Long> promotionIds, Specification<Promotion> spec);
    
}
