package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.promotion.PromotionCreateDTO;
import com.yield.barbershop_backend.dto.promotion.PromotionFilterDTO;
import com.yield.barbershop_backend.model.Promotion;
import com.yield.barbershop_backend.service.PromotionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;





@RestController
@RequestMapping("/promotions")
public class PromotionController {
    
    @Autowired
    PromotionService promotionService;


    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<Promotion>>> getPromotionsByFilter(PromotionFilterDTO param) {
        PagedResponse<Promotion> promotions = new PagedResponse<>(promotionService.getPromotionsByFilter(param));
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotions found successfully", promotions));}
    

    @GetMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<Promotion>> getPromotionById(@PathVariable Long promotionId) {
        Promotion promotion = promotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotion found successfully", promotion));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(@RequestBody @Validated PromotionCreateDTO promotion) {
        Promotion savedPromotion = promotionService.createPromotion(promotion);
        return ResponseEntity.created(null).body(new ApiResponse<>(true, "Promotion created successfully", savedPromotion));
    }

    @PutMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(@PathVariable Long promotionId, @RequestBody PromotionCreateDTO promotion) {
        
        Promotion updatedPromotion = promotionService.updatePromotion(promotionId, promotion);
        return ResponseEntity.ok(new ApiResponse<>(true, "Promotion updated successfully", updatedPromotion));
    }

}
