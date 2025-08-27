package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.DrinkDTO;
import com.yield.barbershop_backend.dto.DrinkFilterDTO;
import com.yield.barbershop_backend.dto.ItemStockQuantityDTO;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.model.Drink;
import com.yield.barbershop_backend.service.DrinkService;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/drinks")
public class DrinkController {
    

    @Autowired
    private DrinkService drinkService;
    
    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<Drink>>> getDrinksByFilter (DrinkFilterDTO filter) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "",
            new PagedResponse<Drink>(drinkService.getDrinksByFilter(filter))
        ));
    }
    
    @GetMapping("/{drinkId}")
    public ResponseEntity<ApiResponse<Drink>> getDrinkById(@PathVariable("drinkId") Long drinkId) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "",
            drinkService.getDrinkById(drinkId)
        ));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Drink>> createDrink(@RequestBody @Validated DrinkDTO drink) {
        Drink createdDrink = drinkService.createDrink(drink);
        return ResponseEntity.created(URI.create("/drinks/" + createdDrink.getDrinkId())).body(new ApiResponse<>(
            true,
            "",
            createdDrink
        ));
    }

    @PutMapping("/{drinkId}")
    public ResponseEntity<ApiResponse<Drink>> updateDrink(@PathVariable Long drinkId, @RequestBody @Validated DrinkDTO drink) {
        Drink updatedDrink = drinkService.updateDrink(drinkId, drink);
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Drink updated successfully",
            updatedDrink
        ));
    }

    @PatchMapping("/{drinkId}/stock")
    public ResponseEntity<ApiResponse<Drink>> updateDrinkStockById(@PathVariable Long drinkId, @RequestBody ItemStockQuantityDTO stockQuantity) {
        drinkService.updateDrinkStock(drinkId, stockQuantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{drinkId}")
    public ResponseEntity<ApiResponse<Void>> deleteDrink(@PathVariable Long drinkId) {
        drinkService.deleteDrink(drinkId);
        return ResponseEntity.noContent().build();
    }



}
