package com.yield.barbershop_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yield.barbershop_backend.model.Service;

import com.yield.barbershop_backend.dto.ApiResponse;
import com.yield.barbershop_backend.dto.PagedResponse;
import com.yield.barbershop_backend.dto.service.ServiceDTO;
import com.yield.barbershop_backend.dto.service.ServiceFilterDTO;
import com.yield.barbershop_backend.service.ServiceService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/services")
public class ServiceController {
    

    @Autowired
    private ServiceService serviceService;


    @GetMapping("")
    public ResponseEntity<ApiResponse<PagedResponse<Service>>> getServicesWithFilter(ServiceFilterDTO filter) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Services fetched successfully",
            new PagedResponse<>(serviceService.getServicesWithFilter(filter))
        ));
    }
    
    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<Service>> getServiceById(@PathVariable Long serviceId) {
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Service fetched successfully",
                serviceService.getServiceById(serviceId)
            ));
    }



    @PostMapping("")
    public ResponseEntity<ApiResponse<Service>> createService(@RequestBody ServiceDTO service) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Service created successfully",
            serviceService.createService(service)
        ));
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<Service>> updateService(@PathVariable Long serviceId, @RequestBody ServiceDTO service) {
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Service updated successfully",
            serviceService.updateService(serviceId, service)
        ));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }

    

}
