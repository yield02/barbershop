package com.yield.barbershop_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yield.barbershop_backend.model.PromotionItem;

public interface PromotionItemRepo extends JpaRepository<PromotionItem, Long> 

{

    void deletePromotionItemsByPromotionId(Long promotionId);
    
} 
