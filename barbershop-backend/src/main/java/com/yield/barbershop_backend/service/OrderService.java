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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.order.OrderCreateDTO;
import com.yield.barbershop_backend.dto.order.OrderFilterDTO;
import com.yield.barbershop_backend.dto.order.OrderProductItemCreateDTO;
import com.yield.barbershop_backend.dto.order.OrderUpdateDTO;
import com.yield.barbershop_backend.exception.DataConflictException;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.AccountPrincipal;
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
    public Order createOrder(OrderCreateDTO order, Boolean isAdmin) {
        
        
        User user = null;
        
        if(isAdmin) {
            user = userService.getUserById(order.getUserId());
        }
        
        
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
            .userId(user.getUserId())
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
    public void updateOrderStatus(Long orderId, String status, Long staffId) {
        // Pending, Processcing, Completed, Cancelled
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));
        
        if(order.getStatus().equals("Completed") || order.getStatus().equals("Cancelled")) {
            throw new DataConflictException("Cannot update status of order with status: " + order.getStatus());
        }

        if(status.equals("Completed")) {
            order.setUserId(staffId);
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

    @Transactional
    public Order updateOrder(Long orderId, OrderUpdateDTO order) {
        
        Order existingOrder = orderRepo.findById(orderId).orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));


        // 1.[Check order status] Start
        if(existingOrder.getStatus().equals("Completed") || existingOrder.getStatus().equals("Cancelled")) {
            throw new DataConflictException("Cannot update order with status: " + existingOrder.getStatus());
        }
        // 1.[Check order status] End

        

            // Get all new drinks and products id
        Map<Long, Long> newDrinkIdsAndQuantity = order.getDrinks().stream().collect(Collectors.toMap(OrderProductItemCreateDTO::getItemId, OrderProductItemCreateDTO::getQuantity));
        Map<Long, Long> newProductIdsAndQuantity = order.getProducts().stream().collect(Collectors.toMap(OrderProductItemCreateDTO::getItemId, OrderProductItemCreateDTO::getQuantity));
        
            // Get all stock drinks and products to check existed and stock
        List<Drink> newDrinks = drinkService.getDrinkByIds(newDrinkIdsAndQuantity.keySet().stream().toList());
        List<Product> newProducts = productService.getProductByIds(newProductIdsAndQuantity.keySet().stream().toList());
        
        
        // 2.[Check new products and drinks id is existed] Start

        Map<String, List<Long>> itemIsNotExisted= new HashMap<>();
        if(newDrinks.size() < order.getDrinks().size()) {
            itemIsNotExisted.put("drinks", newDrinkIdsAndQuantity.keySet().stream().filter(drinkId -> {
                return !newDrinks.stream().map(Drink::getDrinkId).collect(Collectors.toList()).contains(drinkId);
            }).toList());
        }

        if(newProducts.size() < order.getProducts().size()) {
            itemIsNotExisted.put("products", newProductIdsAndQuantity.keySet().stream().filter(productId -> {
                return !newProducts.stream().map(Product::getProductId).collect(Collectors.toList()).contains(productId);
            }).toList());
        }

        if(itemIsNotExisted.size() > 0) {
            throw new DataNotFoundException("Item not found", List.of(itemIsNotExisted));
        }
        // 2.[Check new products and drinks id is existed] End

        // [Check new products and drinks are difference old products and drinks] Start
        List<OrderItem> oldOrderItems = existingOrder.getOrderItems();
            // [divide drink and product from orderItems to be easy to plus for stock] Start
        List<OrderItem> oldDrinkOrderItems = oldOrderItems.stream().filter(item -> item.getDrinkId() != null).collect(Collectors.toList());
        List<OrderItem> oldProducOrdertItems = oldOrderItems.stream().filter(item -> item.getProductId() != null).toList();
            // [divide drink and product from orderItems to be easy to plus for stock] End

        Map<Long, Long> oldDrinkIdsAndQuantity = oldDrinkOrderItems.stream().collect(Collectors.toMap(OrderItem::getDrinkId, OrderItem::getQuantity));
        Map<Long, Long> oldProductIdsAndQuantity = oldProducOrdertItems.stream().collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));

        Boolean isDifferenceItems = true;

        if(oldDrinkIdsAndQuantity.size() == newDrinkIdsAndQuantity.size() && oldProductIdsAndQuantity.size() == newProductIdsAndQuantity.size()) {
            if(oldDrinkIdsAndQuantity.equals(newDrinkIdsAndQuantity) && oldProductIdsAndQuantity.equals(newProductIdsAndQuantity)) {
                isDifferenceItems = false;
            }
        }

        List<OrderItem> newOrderItems = new ArrayList<>();


        // [Check new products and drinks are difference old products and drinks] End

        // If new products and drinks are difference old products and drinks then return quantity to old products and drinks
        // Check stock new products and drinks
        // Delete old orderItems
        // then minus quantity to new products and drinks
        if(isDifferenceItems) {
            // 3.[get orderItems to return the quantity of Product or Drink] Start
            // [get drink and product from id] Start
            List<Drink> oldDrinks = drinkService.getDrinkByIds(oldDrinkIdsAndQuantity.keySet().stream().toList());
            List<Product> oldProducts = productService.getProductByIds(oldProductIdsAndQuantity.keySet().stream().toList());
            // [get drink and product from id] End

            // [Plus Drink Stock, Product Stock and save to database]  Start
            if(oldDrinks.size() > 0) {
                oldDrinks.forEach(drink -> {
                    Long quantity = oldDrinkIdsAndQuantity.get(drink.getDrinkId());
                    drink.setStockQuantity(drink.getStockQuantity() + quantity);
                });
                
                drinkService.saveDrinks(oldDrinks);
            }
            
            if(oldProducts.size() > 0) {
                oldProducts.forEach(product -> {
                    Long quantity = oldProductIdsAndQuantity.get(product.getProductId());
                    product.setStockQuantity(product.getStockQuantity() + quantity);
                });
                productService.saveProducts(oldProducts);
            }
                // [Plus Drink Stock, Product Stock and save to database]  End
                // 3.[get orderItems to return the quantity of Product or Drink] End

                // 4.[Delete order items existed] Start
                    orderItemService.deleteOrderItemsByOrderId(orderId);
                // 4.[Delete order items existed] End
                // 5.[Check order new item is out of stock] Start
                Map<String, List<Long>> itemIsOutOfStock = new HashMap<>();
        
                if(newDrinks.size() > 0) {
        
                    List<Long> drinkIsOutOfStockId = newDrinks.stream().filter(drink -> {
                        Long quantity = newDrinkIdsAndQuantity.get(drink.getDrinkId());
                        return drink.getStockQuantity() < quantity;
                    }).map(Drink::getDrinkId).collect(Collectors.toList());
        
                    if(drinkIsOutOfStockId.size() > 0) {
                        itemIsOutOfStock.put("drinks", drinkIsOutOfStockId);
                    }
                }
        
                if(newProducts.size() > 0) {
                    List<Long> productIsOutOfStockId = newProducts.stream().filter(product -> {
                        Long quantity = newProductIdsAndQuantity.get(product.getProductId());
                        return product.getStockQuantity() < quantity;
                    }).map(Product::getProductId).collect(Collectors.toList());
                    if(productIsOutOfStockId.size() > 0) {   
                        itemIsOutOfStock.put("products", productIsOutOfStockId);
                    }
                }
        
                if(itemIsOutOfStock.size() > 0) {
                    throw new DataNotFoundException("Item out of stock", List.of(itemIsOutOfStock));
                }
        
                // 5.[Check order new item is out of stock] End
        
                // 6. [Minus stock for new item] Start
        
                if(newDrinks.size() > 0) {
                    newDrinks.forEach(drink -> {
                        Long quantity = newDrinkIdsAndQuantity.get(drink.getDrinkId());
        
                        drink.setStockQuantity(drink.getStockQuantity() - quantity);
                    });
                    drinkService.saveDrinks(newDrinks);
                }
        
                if(newProducts.size() > 0) {
                    newProducts.forEach(product -> {
                        Long quantity = newProductIdsAndQuantity.get(product.getProductId());
                        product.setStockQuantity(product.getStockQuantity() - quantity);
                    });
                    productService.saveProducts(newProducts);
                }
                // 6. [Minus stock for new Item] End
                 
        
                // 7. [Create new OrderItems] Start        
                newDrinks.forEach(drink -> {
                    Long quantity = newDrinkIdsAndQuantity.get(drink.getDrinkId());
                    OrderItem newOrderItem = OrderItem.builder()
                    .drinkId(drink.getDrinkId())
                    .quantity(quantity)
                    .orderId(orderId)
                    .name(drink.getDrinkName())
                    .price(drink.getPrice())
                    .build();
                    newOrderItems.add(newOrderItem);
                });
        
                newProducts.forEach(product -> {
                    Long quantity = newProductIdsAndQuantity.get(product.getProductId());
                    OrderItem newOrderItem = OrderItem.builder()
                    .productId(product.getProductId())
                    .quantity(quantity)
                    .orderId(orderId)
                    .name(product.getProductName())
                    .price(product.getPrice())
                    .build();
                    newOrderItems.add(newOrderItem);
                });
        
                orderItemService.createOrderItems(newOrderItems);
                // 7. [Create new OrderItems] End
        }


        // 8.[Update order status and updateTime] Start

        if(order.getCustomerId() != existingOrder.getCustomer().getCustomerId()) {
            Customer newCustomer = customerService.getCustomerById(order.getCustomerId());
            existingOrder.setCustomer(newCustomer);
            existingOrder.setCustomerName(newCustomer.getFullName());
            existingOrder.setCustomerEmail(newCustomer.getEmail());
            existingOrder.setCustomerPhone(newCustomer.getPhoneNumber());
        }

        existingOrder.setNotes(order.getNotes());
        existingOrder.setUpdatedAt(new Date(System.currentTimeMillis()));
        Order savedOrder = orderRepo.save(existingOrder); 
        if(isDifferenceItems) {
            savedOrder.setOrderItems(newOrderItems);
        }
        // 8.[Update order status and updateTime] End

        return savedOrder;
    }


    @Transactional
    public void cancelOrder(Long orderId, Long ownerId, Boolean isAdmin) {
        
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        // 1.[Check order status] START
        if(order.getStatus().equals("Cancelled") || order.getStatus().equals("Completed")) {
            throw new DataConflictException("Cannot update status of order with status: " + order.getStatus());
        }
        // 1.[Check order status] END

        // 2.[Check order owner or staff] START
        if(!isAdmin) {
            if(!order.getCustomer().getId().equals(ownerId)) {
                throw new AccessDeniedException("You don't have permission to cancel this order");
            }
        }
        // 2.[Check order owner or staff] END

        // 3. [Return quantity to stock] START

        List<OrderItem> orderItems = order.getOrderItems();
        
        List<Long> drinkIds = orderItems.stream().filter(item -> item.getDrinkId() != null).map(OrderItem::getDrinkId).collect(Collectors.toList());
        List<Long> productIds = orderItems.stream().filter(item -> item.getProductId() != null).map(OrderItem::getProductId).collect(Collectors.toList());

        List<Drink> drinks = drinkService.getDrinkByIds(drinkIds);
        List<Product> products = productService.getProductByIds(productIds);

        drinks.forEach(drink -> {
            Long quantity = orderItems.stream()
                .filter(item -> item.getDrinkId() != null && item.getDrinkId().equals(drink.getDrinkId()))
                .map(OrderItem::getQuantity)
                .findFirst()
                .orElse(0L);
            drink.setStockQuantity(drink.getStockQuantity() + quantity);
        });
        drinkService.saveDrinks(drinks);

        products.forEach(product -> {
            Long quantity = orderItems.stream()
                .filter(item -> item.getProductId() != null && item.getProductId().equals(product.getProductId()))
                .map(OrderItem::getQuantity)
                .findFirst()
                .orElse(0L);
            product.setStockQuantity(product.getStockQuantity() + quantity);
        });
        productService.saveProducts(products);

        // 3. [Return quantity to stock] END


        // 4.[Update order status and updateTime] START
        order.setStatus("Cancelled");
        order.setUpdatedAt(new Date(System.currentTimeMillis()));        
        orderRepo.save(order);
        // 4.[Update order status and updateTime] END
    }
}

