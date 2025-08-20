package com.yield.barbershop_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.dto.DrinkDTO;
import com.yield.barbershop_backend.dto.DrinkFilterDTO;
import com.yield.barbershop_backend.dto.ItemStockQuantityDTO;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Drink;
import com.yield.barbershop_backend.repository.DrinkRepo;
import com.yield.barbershop_backend.specification.DrinkSpecification;

import jakarta.transaction.Transactional;

@Service
public class DrinkService {


    @Autowired
    private DrinkRepo drinkRepo;


    public Page<Drink> getDrinksByFilter(DrinkFilterDTO filter) {

        Pageable page = PageRequest.of(filter.getPage(), filter.getPageSize());

        return drinkRepo.findAll(DrinkSpecification.filters(filter), page);
    }


    public Drink getDrinkById(Long drinkId) throws DataNotFoundException {
        return drinkRepo.findById(drinkId)
                .orElseThrow(() -> new DataNotFoundException("Drink not found"));
    }


    public Drink createDrink(DrinkDTO drink) {
        return drinkRepo.save(new Drink(drink));
    }


    public Drink updateDrink(Long drinkId, DrinkDTO drink) {
        return drinkRepo.save(new Drink(drinkId, drink));
    }


    public void deleteDrink(Long drinkId) {
        drinkRepo.deleteById(drinkId);
    }

    @Transactional
    public void updateDrinkStock(Long drinkId, ItemStockQuantityDTO stockQuantity) {
        int result = drinkRepo.updateDrinkStockById(drinkId, stockQuantity.getStockQuantity());
        if (result == 0) {
            throw new DataNotFoundException("Drink not found");
        }
    }
    
}   