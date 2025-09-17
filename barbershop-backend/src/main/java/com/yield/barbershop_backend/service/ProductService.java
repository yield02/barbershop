package com.yield.barbershop_backend.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.yield.barbershop_backend.dto.product.ItemStockQuantityDTO;
import com.yield.barbershop_backend.dto.product.ProductDTO;
import com.yield.barbershop_backend.dto.product.ProductFilterDTO;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Product;
import com.yield.barbershop_backend.repository.ProductRepo;
import com.yield.barbershop_backend.specification.ProductSpecification;

@Service
public class ProductService {
    
  
    @Autowired
    private ProductRepo productRepo;

    public Product getProductById(Long productId) throws DataNotFoundException {
        return productRepo.findById(productId).orElseThrow(() -> new DataNotFoundException("Product not found"));
    }

    public Page<Product> getProductsByFilter(@RequestParam ProductFilterDTO filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getPageSize());
        return productRepo.findAll(ProductSpecification.filters(filter), pageable);

    }

    public Product createProduct(ProductDTO product) {
        return productRepo.save(new Product(product));
    }

    public void deleteProduct(Long productId) {
        productRepo.deleteById(productId);

    }

        // This approach will affect system performance because it calls the database twice.
    // public void deleteProduct(Long productId) {
    //     try {
    //         productRepo.deleteById(productId);
    //     } catch (EmptyResultDataAccessException e) {
    //         throw new DataNotFoundException("Product not found for deletion");
    //     }
    // }

    public Product updateProduct(Long productId, ProductDTO product) {
        return productRepo.save(new Product(productId, product));
    }

    @Transactional
    public void updateProductStock(Long productId, ItemStockQuantityDTO stockQuantity) {
        int updatedRows = productRepo.updateProductStockById(productId, stockQuantity.getStockQuantity());
        if (updatedRows == 0) {
            throw new DataNotFoundException("Product not found for stock update");
        }
    }

    public List<Product> getProductByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return productRepo.getProductByIds(productIds);
    }


    public void saveProducts(List<Product> products) {
        productRepo.saveAll(products);
    }


}
