package com.yield.barbershop_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.promotion.PromotionCreateDTO;
import com.yield.barbershop_backend.dto.promotion.promotionItemCreateDTO.PromotionItemType;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Drink;
import com.yield.barbershop_backend.model.Product;
import com.yield.barbershop_backend.model.Promotion;
import com.yield.barbershop_backend.model.PromotionItem;
import com.yield.barbershop_backend.model.Service;
import com.yield.barbershop_backend.repository.PromotionRepo;
import com.yield.barbershop_backend.specification.PromotionSpecification;
import com.yield.barbershop_backend.util.ListUtil;

@org.springframework.stereotype.Service
public class PromotionService {
    
    @Autowired
    private PromotionRepo promotionRepo;

    @Autowired PromotionItemService promotionItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private DrinkService drinkService;

    public List<Promotion> getActivePromotionsByIds(List<Long> promotionIds) {
        return promotionRepo.getActivePromotionsByIds(promotionIds, PromotionSpecification.getActivePromotions());
    }

    @Transactional
    public Promotion createPromotion(PromotionCreateDTO promotion) {


        List<Long> productIds = new ArrayList<>();
        List<Long> serviceIds = new ArrayList<>();
        List<Long> drinkIds = new ArrayList<>();

        promotion.getPromotionItems().forEach(item -> {
            if(item.getItemType() == PromotionItemType.SERVICE.toString()) {
                serviceIds.add(item.getItemId());
            }

            if(item.getItemType() == PromotionItemType.PRODUCT.toString()) {
                productIds.add(item.getItemId());
            }

            if(item.getItemType() == PromotionItemType.DRINK.toString()) {
                drinkIds.add(item.getItemId());
            }
        });


        List<Product> products = productService.getActiveProductByIds(productIds);
        List<Service> services = serviceService.getActiveServicesByIds(serviceIds);
        List<Drink> drinks = drinkService.getActiveDrinkByIds(drinkIds);

        Map<String, List<Long>> itemIsNotExisted = new HashMap<>();

        if(productIds.size() > products.size()) {
            List <Long> productIsNotExisted = ListUtil.difference(productIds, products.stream().map(Product::getProductId).toList());
            itemIsNotExisted.put("products", productIsNotExisted);
        }

        if(serviceIds.size() > services.size()) {
            List <Long> serviceIsNotExisted = ListUtil.difference(serviceIds, services.stream().map(Service::getServiceId).toList());
            itemIsNotExisted.put("services", serviceIsNotExisted);
        }

        if(drinkIds.size() > drinks.size()) {
            List <Long> drinkIsNotExisted = ListUtil.difference(drinkIds, drinks.stream().map(Drink::getDrinkId).toList());
            itemIsNotExisted.put("drinks", drinkIsNotExisted);
        }

        if(!itemIsNotExisted.isEmpty()) {
            throw new DataNotFoundException("Item is not existed", List.of(itemIsNotExisted));
        }

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
            if(item.getItemType() == PromotionItemType.SERVICE.toString()) {
                promotionItem.setServiceId(item.getItemId());
            }

            if(item.getItemType() == PromotionItemType.PRODUCT.toString()) {
                promotionItem.setProductId(item.getItemId());
            }

            if(item.getItemType() == PromotionItemType.DRINK.toString()) {
                promotionItem.setDrinkId(item.getItemId());
            }

            promotionItem.setPromotionId(savedPromotion.getPromotionId());

            promotionItems.add(promotionItem);
        });
        
        promotionItemService.savePromotionItems(promotionItems);

        return savedPromotion;
    }

    public Promotion getPromotionById(Long promotionId) {
        
        Promotion promotion = promotionRepo.findById(promotionId)
        .orElseThrow(() -> new DataNotFoundException("Promotion not found"));
    
        return promotion;
    }

}

