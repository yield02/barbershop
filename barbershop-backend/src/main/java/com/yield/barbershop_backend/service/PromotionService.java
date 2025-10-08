package com.yield.barbershop_backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.model.Promotion;
import com.yield.barbershop_backend.repository.PromotionRepo;
import com.yield.barbershop_backend.specification.PromotionSpecification;

@Service
public class PromotionService {
    
    @Autowired
    private PromotionRepo promotionRepo;

    public Map<Long, Promotion> getActivePromotionsByIds(List<Long> promotionIds) {
        return promotionRepo.getActivePromotionsByIds(promotionIds, PromotionSpecification.getActivePromotions());
    }

}

