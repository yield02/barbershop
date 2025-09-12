package com.yield.barbershop_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yield.barbershop_backend.dto.order.OrderFilterDTO;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.repository.OrderRepo;
import com.yield.barbershop_backend.specification.OrderSpecification;


@Service
public class OrderService {


    @Autowired
    private OrderRepo orderRepo;
    
    public Order getOrderById(Long id) {
        return orderRepo
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
    }

    public Page<Order> getOrdersByFilter(OrderFilterDTO filter) {
        Pageable page = PageRequest.of(filter.getPage(), filter.getPageSize());
        return orderRepo.findAll(OrderSpecification.getOrderWithFilter(filter), page);
    }
    
}
