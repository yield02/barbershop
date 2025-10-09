package com.yield.barbershop_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.promotion.PromotionCreateDTO;
import com.yield.barbershop_backend.dto.promotion.promotionItemCreateDTO.PromotionItemType;
import com.yield.barbershop_backend.model.Promotion;
import com.yield.barbershop_backend.model.PromotionItem;
import com.yield.barbershop_backend.repository.PromotionRepo;
import com.yield.barbershop_backend.specification.PromotionSpecification;

@Service
public class PromotionService {
    
    @Autowired
    private PromotionRepo promotionRepo;

    @Autowired PromotionItemService promotionItemService;

    public Map<Long, Promotion> getActivePromotionsByIds(List<Long> promotionIds) {
        return promotionRepo.getActivePromotionsByIds(promotionIds, PromotionSpecification.getActivePromotions());
    }

    @Transactional
    public Promotion createPromotion(PromotionCreateDTO promotion) {


        Promotion entity = new Promotion();

        entity.setPromotionName(promotion.getPromotionName());
        entity.setDescription(promotion.getDescription());
        entity.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity());

        entity.setStartDate(promotion.getStartDate());
        entity.setEndDate(promotion.getEndDate());
        entity.setIsActive(promotion.getIsActive());
        entity.setCreatedAt(promotion.getCreatedAt());
        entity.setUpdatedAt(promotion.getUpdatedAt());

        
        if(promotion.getDiscountAmount() != null) {
            entity.setDiscountAmount(promotion.getDiscountAmount());
            entity.setDiscountPercentage(null);
        }
        else {
            entity.setDiscountPercentage(promotion.getDiscountPercentage());
            entity.setDiscountAmount(null);
        }

        Promotion savedPromotion = promotionRepo.save(entity);
        
        List<PromotionItem> promotionItems = new ArrayList<>();

        promotion.getPromotionItems().forEach(item -> {
            PromotionItem promotionItem = new PromotionItem();
            if(item.getItemType() == PromotionItemType.SERVICE) {
                promotionItem.setServiceId(item.getItemId());
            }

            if(item.getItemType() == PromotionItemType.PRODUCT) {
                promotionItem.setProductId(item.getItemId());
            }

            if(item.getItemType() == PromotionItemType.DRINK) {
                promotionItem.setDrinkId(item.getItemId());
            }

            promotionItem.setPromotionId(savedPromotion.getPromotionId());

            promotionItems.add(promotionItem);
        });
        
        promotionItemService.savePromotionItems(promotionItems);

        return savedPromotion;
    }

}

