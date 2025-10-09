package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.promotion.PromotionCreateDTO;
import com.yield.barbershop_backend.model.Promotion;
import com.yield.barbershop_backend.service.PromotionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/promotions")
public class PromotionController {
    
    @Autowired
    PromotionService promotionService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(@RequestBody @Validated PromotionCreateDTO promotion) {
        
        Promotion savedPromotion = promotionService.createPromotion(promotion);


        return ResponseEntity.created(null).body(new ApiResponse<>(true, "Promotion created successfully", savedPromotion));
    }
    


}
