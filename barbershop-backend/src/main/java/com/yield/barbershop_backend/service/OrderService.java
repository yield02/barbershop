package com.yield.barbershop_backend.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.order.OrderCreateDTO;
import com.yield.barbershop_backend.dto.order.OrderFilterDTO;
import com.yield.barbershop_backend.exception.DataConflictException;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.model.Drink;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.model.OrderItem;
import com.yield.barbershop_backend.model.Product;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.repository.OrderRepo;
import com.yield.barbershop_backend.specification.OrderSpecification;


@Service
public class OrderService {


    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DrinkService drinkService;

    @Autowired
    private OrderItemService orderItemService;

    
    public Order getOrderById(Long id) {
        return orderRepo
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
    }

    public Page<Order> getOrdersByFilter(OrderFilterDTO filter) {
        Pageable page = PageRequest.of(filter.getPage(), filter.getPageSize());
        return orderRepo.findAll(OrderSpecification.getOrderWithFilter(filter), page);
    }

    @Transactional
    public Order createOrder(OrderCreateDTO order) {
        
        User user = userService.getUserById(order.getUserId());
        
        Customer customer = customerService.getCustomerById(order.getCustomerId());
        
        
        List<Long> drinkIds = order.getDrinks().stream().map((drink) -> drink.getItemId()).toList();
        List<Long> productIds = order.getProducts().stream().map((product) -> product.getItemId()).toList();
        
        
        List<Product> products = productService.getProductByIds(productIds);
        List<Drink> drinks = drinkService.getDrinkByIds(drinkIds);

        
        Map<String, List<Long>> itemsNotFound = new HashMap<>();
        
        if(drinkIds.size() != drinks.size()) {
            List<Long> drinkIsNotExisted = drinkIds.stream()
            .filter(id -> !drinks.stream()
            .map(Drink::getDrinkId)
            .anyMatch(drinkId -> drinkId.equals(id)))
            .collect(Collectors.toList());
            
            itemsNotFound.put("Drinks not found with ids", drinkIsNotExisted);
        }

        if(productIds.size() != products.size()) {
            List<Long> productIsNotExisted = productIds.stream()
            .filter(id -> !products.stream()
                .map(Product::getProductId)
                .anyMatch(productId -> productId.equals(id)))
            .collect(Collectors.toList());
            itemsNotFound.put("Products not found with ids", productIsNotExisted);
        }

        if(itemsNotFound.size() > 0) {
            throw new DataNotFoundException("Item not found", Arrays.asList(itemsNotFound));
        }

        
        // Check stock
        List<Long> drinkIsOutOfStock = new ArrayList<>();
        List<Long> productIsOutOfStock = new ArrayList<>();
        
        
        if(order.getDrinks() != null || order.getDrinks().size() > 0) {
            order.getDrinks().stream().forEach(drink -> {
                Long drinkQuantity = drink.getQuantity();
                Long stockQuantity = drinks.stream().filter(drinkItem -> drinkItem.getDrinkId().equals(drink.getItemId())).findFirst().get().getStockQuantity();
                if(drinkQuantity > stockQuantity) {
                    drinkIsOutOfStock.add(drink.getItemId());
                }
            });
        }
        
        if(order.getProducts() != null || order.getProducts().size() > 0) {
            order.getProducts().stream().forEach(product -> {
                Long productQuantity = product.getQuantity();
                Long stockQuantity = products.stream().filter(productItem -> productItem.getProductId().equals(product.getItemId())).findFirst().get().getStockQuantity();
                if(productQuantity > stockQuantity) {
                    productIsOutOfStock.add(product.getItemId());
                }
            });
        }
        
    
        if(drinkIsOutOfStock.size() > 0 || productIsOutOfStock.size() > 0) {
            Map<String, List<Long>> data = new HashMap<>();
            data.put("drinkIsOutOfStock", drinkIsOutOfStock);
            data.put("productIsOutOfStock", productIsOutOfStock);
            throw new DataConflictException("Item is out of stock", Arrays.asList(data));
        }




        // Minus Stock

        // Minus Drink Stock
        if(drinks.size() > 0) {
            drinks.forEach(drink -> {
            Long quantity = order.getDrinks().stream().filter(drinkItem -> drinkItem.getItemId().equals(drink.getDrinkId())).findFirst().get().getQuantity();
            drink.setStockQuantity(drink.getStockQuantity() - quantity);
            });
            drinkService.saveDrinks(drinks);
        }
        
        
        // Minus Product Stock
        if(products.size() > 0) {
            products.forEach(product -> {
            Long quantity = order.getProducts().stream().filter(productItem -> productItem.getItemId().equals(product.getProductId())).findFirst().get().getQuantity();
            product.setStockQuantity(product.getStockQuantity() - quantity);
            });
            productService.saveProducts(products);
        }

        // Calculate totalAmount
        Double totalPriceProduct = products.stream().mapToDouble(product -> {
            Long quantity = order.getProducts().stream().filter(productItem -> productItem.getItemId().equals(product.getProductId())).findFirst().get().getQuantity();
            return product.getPrice() * quantity;
        }).sum();

        Double totalPriceDrink = drinks.stream().mapToDouble(drink -> {
            Long quantity = order.getDrinks().stream().filter(drinkItem -> drinkItem.getItemId().equals(drink.getDrinkId())).findFirst().get().getQuantity();
            return drink.getPrice() * quantity;
        }).sum();

        
        Double totalAmount = totalPriceDrink + totalPriceProduct;

        // Create and Save Order
        Order orderCreate = Order.builder()
            .customer(customer)
            .user(user)
            .notes(order.getNotes())
            .customerName(customer.getFullName())
            .customerEmail(customer.getEmail())
            .customerPhone(customer.getPhoneNumber())
            .totalAmount(totalAmount)
            .status("Pending")
            .createdAt(new Date(System.currentTimeMillis()))
            .updatedAt(new Date(System.currentTimeMillis()))
            .build();

   
        Order savedOrder = orderRepo.save(orderCreate);

        System.out.println("Saved Order: " + savedOrder.getOrderId());


        // Create and Save OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.addAll(drinks.stream().map(drink -> {
            OrderItem orderItem = OrderItem.builder()
                .orderId(savedOrder.getOrderId())
                .drinkId(drink.getDrinkId())
                .name(drink.getDrinkName())
                .quantity(order.getDrinks().stream().filter(drinkItem -> drinkItem.getItemId().equals(drink.getDrinkId())).findFirst().get().getQuantity())
                .price(drink.getPrice())
                .build();
            return orderItem;
        }).toList());

        orderItems.addAll(products.stream().map(product -> {
            OrderItem orderItem = OrderItem.builder()
                .orderId(savedOrder.getOrderId())
                .productId(product.getProductId())
                .name(product.getProductName())
                .quantity(order.getProducts().stream().filter(productItem -> productItem.getItemId().equals(product.getProductId())).findFirst().get().getQuantity())
                .price(product.getPrice())
                .build();
            return orderItem;
        }).toList());

        List<OrderItem> savedOrderItems= orderItemService.createOrderItems(orderItems);
        
        savedOrder.setOrderItems(savedOrderItems);

        return savedOrder;
    }
    
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        // Pending, Processcing, Completed, Cancelled
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));
        
        if(order.getStatus().equals("Completed") || order.getStatus().equals("Cancelled")) {
            throw new DataConflictException("Cannot update status of order with status: " + order.getStatus());
        }

        order.setStatus(status);
        order.setUpdatedAt(new Date(System.currentTimeMillis()));
        
        // Save order
        orderRepo.save(order);

        if(!order.getStatus().equals("Cancelled")) {
            return ;
        }
        
        // Plus Stock
        List<OrderItem> items = order.getOrderItems();

        List<OrderItem> drinkItems = items.stream().filter(item -> item.getDrinkId() != null).collect(Collectors.toList());
        List<OrderItem> productItems = items.stream().filter(item -> item.getProductId() != null).toList();


        List<Long> drinkIds = drinkItems.stream().map(OrderItem::getDrinkId).collect(Collectors.toList());
        List<Long> productIds = productItems.stream().map(OrderItem::getProductId).toList();

        List<Drink> drinks = drinkService.getDrinkByIds(drinkIds);
        List<Product> products = productService.getProductByIds(productIds);
        
        // Plus Drink Stock
        if(drinks.size() > 0) {
            drinks.forEach(drink -> {
            Long quantity = drinkItems.stream().filter(drinkItem -> drinkItem.getDrinkId().equals(drink.getDrinkId())).findFirst().get().getQuantity();
            drink.setStockQuantity(drink.getStockQuantity() + quantity);
            });
            drinkService.saveDrinks(drinks);
        }
        
        
        // Plus Product Stock
        if(products.size() > 0) {
            products.forEach(product -> {
            Long quantity = productItems.stream().filter(productItem -> productItem.getProductId().equals(product.getProductId())).findFirst().get().getQuantity();
            product.setStockQuantity(product.getStockQuantity() + quantity);
            });
            productService.saveProducts(products);
        }
    }
}

