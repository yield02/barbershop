package com.yield.barbershop_backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.product.ItemStockQuantityDTO;
import com.yield.barbershop_backend.dto.product.ProductDTO;
import com.yield.barbershop_backend.dto.product.ProductFilterDTO;
import com.yield.barbershop_backend.model.Product;
import com.yield.barbershop_backend.service.ProductService;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;





@RestController
@RequestMapping("/products")
public class ProductController {
    

    @Autowired
    ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long productId)  {
        
        return ResponseEntity.ok(new ApiResponse<Product>(
            true,
            "",
            productService.getProductById(productId)
        ));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<Product>>> getProductsByFilter(ProductFilterDTO filter) {

        System.out.println("Filter: " + filter);

        return ResponseEntity.ok(new ApiResponse<PagedResponse<Product>>(
            true,
            "",
            new PagedResponse<>(productService.getProductsByFilter(filter))
        ));
    }
    
    @PostMapping("")
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody @Validated ProductDTO product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.created(URI.create("/products/" + createdProduct.getProductId())).body(new ApiResponse<Product>(
            true,
            "Product created successfully",
            createdProduct
        ));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long productId, @RequestBody @Validated ProductDTO product) {
        System.out.println("Updating product: " + product);
        Product updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.ok(new ApiResponse<Product>(
            true,
            "Product updated successfully",
            updatedProduct
        ));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<Product>> updateProductStockById(@PathVariable Long productId, @RequestBody ItemStockQuantityDTO stockQuantity) {
        productService.updateProductStock(productId, stockQuantity);
        return ResponseEntity.noContent().build();
    }



    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(new ApiResponse<Void>(
            true,
            "Product deleted successfully",
            null
        ));
    }

}