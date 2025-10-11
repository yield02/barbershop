package com.yield.barbershop_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Drink;

@Repository
public interface DrinkRepo extends 
JpaRepository<Drink, Long>,
PagingAndSortingRepository<Drink, Long>,
JpaSpecificationExecutor<Drink> 
{
    // Additional methods for DrinkRepo can be defined here if needed
    //    @Modifying(flushAutomatically = true, clearAutomatically = true)
//    @Query("UPDATE products p SET p.stockQuantity = :stockQuantity WHERE p.productId = :id")
//    int updateProductStockById(@Param("id") Long id, @Param("stockQuantity") Integer stockQuantity);

    @Query("UPDATE drinks d SET d.stockQuantity = :stockQuantity WHERE d.drinkId = :id")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    int updateDrinkStockById(@Param("id") Long id, @Param("stockQuantity") Integer stockQuantity);


    @EntityGraph(attributePaths = {"promotionItems"})
    @Query("SELECT d FROM drinks d WHERE d.drinkId IN :drinkIds AND d.isActive = true")
    List<Drink> getActiveDrinkByIds(List<Long> drinkIds);
}


