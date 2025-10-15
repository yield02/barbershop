package com.yield.barbershop_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.promotion.PromotionCreateDTO;
import com.yield.barbershop_backend.dto.promotion.PromotionFilterDTO;
import com.yield.barbershop_backend.dto.promotion.PromotionItemCreateDTO;
import com.yield.barbershop_backend.dto.promotion.PromotionItemCreateDTO.PromotionItemType;
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


        Set<Long> productIds = new HashSet<>();
        Set<Long> serviceIds = new HashSet<>();
        Set<Long> drinkIds = new HashSet<>();

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
            List <Long> productIsNotExisted = ListUtil.difference(productIds.stream().toList(), products.stream().map(Product::getProductId).toList());
            itemIsNotExisted.put("products", productIsNotExisted);
        }

        if(serviceIds.size() > services.size()) {
            List <Long> serviceIsNotExisted = ListUtil.difference(serviceIds.stream().toList(), services.stream().map(Service::getServiceId).toList());
            itemIsNotExisted.put("services", serviceIsNotExisted);
        }

        if(drinkIds.size() > drinks.size()) {
            List <Long> drinkIsNotExisted = ListUtil.difference(drinkIds.stream().toList(), drinks.stream().map(Drink::getDrinkId).toList());
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

    public Page<Promotion> getPromotionsByFilter(PromotionFilterDTO filter) {
        Pageable page = PageRequest.of(filter.getPage(), filter.getPageSize());
        return promotionRepo.findAll(PromotionSpecification.getPromotionsByFilter(filter), page);
    }


    @Transactional
    public Promotion updatePromotion(Long promotionId, PromotionCreateDTO promotion) {

        Promotion dbPromotion = promotionRepo.findById(promotionId)
        .orElseThrow(() -> new DataNotFoundException("Promotion not found"));

        dbPromotion.setPromotionName(promotion.getPromotionName());
        dbPromotion.setDescription(promotion.getDescription());
        dbPromotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity());

        dbPromotion.setStartDate(promotion.getStartDate());
        dbPromotion.setEndDate(promotion.getEndDate());
        dbPromotion.setIsActive(promotion.getIsActive());
        dbPromotion.setUpdatedAt(promotion.getUpdatedAt());

        if(promotion.getDiscountAmount() != null) {
            dbPromotion.setDiscountAmount(promotion.getDiscountAmount());
            dbPromotion.setDiscountPercentage(null);
        }
        else if(promotion.getDiscountPercentage() != null) {
            dbPromotion.setDiscountPercentage(promotion.getDiscountPercentage());
            dbPromotion.setDiscountAmount(null);
        }
        Promotion updatedPromotion = promotionRepo.save(dbPromotion);

        Set<Long> productIds = new HashSet<>();
        Set<Long> serviceIds = new HashSet<>();
        Set<Long> drinkIds = new HashSet<>();

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
            List <Long> productIsNotExisted = ListUtil.difference(productIds.stream().toList(), products.stream().map(Product::getProductId).toList());
            itemIsNotExisted.put("products", productIsNotExisted);
        }

        if(serviceIds.size() > services.size()) {
            List <Long> serviceIsNotExisted = ListUtil.difference(serviceIds.stream().toList(), services.stream().map(Service::getServiceId).toList());
            itemIsNotExisted.put("services", serviceIsNotExisted);
        }

        if(drinkIds.size() > drinks.size()) {
            List <Long> drinkIsNotExisted = ListUtil.difference(drinkIds.stream().toList(), drinks.stream().map(Drink::getDrinkId).toList());
            itemIsNotExisted.put("drinks", drinkIsNotExisted);
        }

        if(!itemIsNotExisted.isEmpty()) {
            throw new DataNotFoundException("Item is not existed", List.of(itemIsNotExisted));
        }

        List<PromotionItem> oldPromotionItems = dbPromotion.getPromotionItems();
        Set<Long> oldProductPromotionIds = new HashSet<>();
        Set<Long> oldServicePromotionIds = new HashSet<>();
        Set<Long> oldDrinkPromotionIds = new HashSet<>();

        oldPromotionItems.forEach(item -> {
            if(item.getProductId() != null) {
                oldProductPromotionIds.add(item.getProductId());
            }
            else if(item.getServiceId() != null) {
                oldServicePromotionIds.add(item.getServiceId());
            }
            else if(item.getDrinkId() != null) {
                oldDrinkPromotionIds.add(item.getDrinkId());
            }
        });

        List<PromotionItemCreateDTO> newPromotionItems = promotion.getPromotionItems();

        Boolean isSame = true;

        if(oldPromotionItems.size() == promotion.getPromotionItems().size()) {

            for(PromotionItemCreateDTO item : newPromotionItems) {
                if(item.getItemType() == PromotionItemType.SERVICE.toString()) {
                    if(!oldServicePromotionIds.contains(item.getItemId())) {
                        isSame = false;
                        break;
                    }
                }
                else if(item.getItemType() == PromotionItemType.PRODUCT.toString()) {
                    if(!oldProductPromotionIds.contains(item.getItemId())) {
                        isSame = false;
                        break;
                    }
                }
                else if(item.getItemType() == PromotionItemType.DRINK.toString()) {
                    if(!oldDrinkPromotionIds.contains(item.getItemId())) {
                        isSame = false;
                        break;
                    }
                }
            }
        }
        else {
            isSame = false;
        }

        if(!isSame) {

            promotionItemService.deletePromotionItemsByPromotionId(promotionId);

            List<PromotionItem> promotionItems = new ArrayList<>();

            if(productIds.size() > 0) {
                promotionItems.addAll(productIds.stream().map(productId -> {
                    PromotionItem promotionItem = new PromotionItem();
                    promotionItem.setProductId(productId);
                    promotionItem.setPromotionId(promotionId);
                    return promotionItem;
                }).toList());
            }

            if(serviceIds.size() > 0) {
                promotionItems.addAll(serviceIds.stream().map(serviceId -> {
                    PromotionItem promotionItem = new PromotionItem();
                    promotionItem.setServiceId(serviceId);
                    promotionItem.setPromotionId(promotionId);
                    return promotionItem;
                }).toList());
            }

            if(drinkIds.size() > 0) {
                promotionItems.addAll(drinkIds.stream().map(drinkId -> {
                    PromotionItem promotionItem = new PromotionItem();
                    promotionItem.setDrinkId(drinkId);
                    promotionItem.setPromotionId(promotionId);
                    return promotionItem;
                }).toList());
            }

            promotionItemService.savePromotionItems(promotionItems);
        }
        return updatedPromotion;
    }

}

