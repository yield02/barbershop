package com.yield.barbershop_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yield.barbershop_backend.model.Product;

@Repository
public interface ProductRepo extends 
   JpaRepository<Product, Long>, 
   PagingAndSortingRepository<Product, Long>, 
   JpaSpecificationExecutor<Product> 
{
   Optional<Product> findById(Long id);

   @Modifying(flushAutomatically = true, clearAutomatically = true)
   @Query("UPDATE products p SET p.stockQuantity = :stockQuantity WHERE p.productId = :id")
   int updateProductStockById(@Param("id") Long id, @Param("stockQuantity") Integer stockQuantity);

}
