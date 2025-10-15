package com.yield.barbershop_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.model.PromotionItem;
import com.yield.barbershop_backend.repository.PromotionItemRepo;

@Service
public class PromotionItemService {
    
    @Autowired
    PromotionItemRepo promotionItemRepo;

    public void savePromotionItems(List<PromotionItem> promotionItems) {
        promotionItemRepo.saveAll(promotionItems);
    }


}
