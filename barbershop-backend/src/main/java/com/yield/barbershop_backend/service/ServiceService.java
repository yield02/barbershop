package com.yield.barbershop_backend.service;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.yield.barbershop_backend.dto.service.ServiceDTO;
import com.yield.barbershop_backend.dto.service.ServiceFilterDTO;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Service;
import com.yield.barbershop_backend.repository.ServiceRepo;
import com.yield.barbershop_backend.specification.ServiceSpecification;

import jakarta.transaction.Transactional;

@org.springframework.stereotype.Service
public class ServiceService {
    

    @Autowired
    private ServiceRepo serviceRepo;

    public Page<Service> getServicesWithFilter(ServiceFilterDTO filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getPageSize());
        return serviceRepo.findAll(ServiceSpecification.filter(filter), pageable);  
    }

    public Service getServiceById(Long serviceId) throws DataNotFoundException {
        return serviceRepo.findById(serviceId)
            .orElseThrow(() -> new DataNotFoundException("Service not found"));
    }

    public Service createService(ServiceDTO service) {
        Service newService = new Service(service);
        return serviceRepo.save(newService);
    }

    public void deleteService(Long serviceId) {
        serviceRepo.deleteById(serviceId);
    }

    public Service updateService(Long serviceId, ServiceDTO service) {
        return serviceRepo.save(new Service(serviceId, service));
    }

    public List<Service> findExistedServiceIds(List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return List.of();
        }
        // Truy vấn 1 lần duy nhất tất cả các id tồn tại
        List<Service> existedIds = serviceRepo.findExistedIds(serviceIds);
        return existedIds;
    }


}
