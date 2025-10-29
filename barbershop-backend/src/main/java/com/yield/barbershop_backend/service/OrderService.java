package com.yield.barbershop_backend.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yield.barbershop_backend.dto.order.OrderCreateDTO;
import com.yield.barbershop_backend.dto.order.OrderFilterDTO;
import com.yield.barbershop_backend.dto.order.OrderProductItemCreateDTO;
import com.yield.barbershop_backend.dto.order.OrderUpdateDTO;
import com.yield.barbershop_backend.exception.DataConflictException;
import com.yield.barbershop_backend.exception.DataNotFoundException;
import com.yield.barbershop_backend.model.Customer;
import com.yield.barbershop_backend.model.Drink;
import com.yield.barbershop_backend.model.Order;
import com.yield.barbershop_backend.model.OrderItem;
import com.yield.barbershop_backend.model.Product;
import com.yield.barbershop_backend.model.Promotion;
import com.yield.barbershop_backend.model.PromotionItem;
import com.yield.barbershop_backend.model.User;
import com.yield.barbershop_backend.model.Order.OrderStatus;
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

    @Autowired
    private PromotionService promotionService;

    public Order getOrderById(Long id) {
        return orderRepo
                .findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
    }

    public Page<Order> getOrdersByFilter(OrderFilterDTO filter) {
        Pageable page = PageRequest.of(filter.getPage(), filter.getPageSize());
        return orderRepo.findAll(OrderSpecification.getOrderWithFilter(filter), page);
    }

    public List<Order> getOrdersWithSpecification(Specification<Order> spec) {
        return orderRepo.findAll(spec);
    }

    @Transactional
    public Order createOrder(OrderCreateDTO order, Boolean isAdmin) {

        User user = null;

        if (isAdmin) {
            user = userService.getUserById(order.getUserId());
        }
        // <productId, ProductQuantity>
        Map<Long, Long> orderProductQuantityMap = order.getProducts().stream()
                .collect(Collectors.toMap(OrderProductItemCreateDTO::getItemId, item -> item.getQuantity()));
        // <drinkId, DrinkQuantity>
        Map<Long, Long> orderDrinkQuantityMap = order.getDrinks().stream()
                .collect(Collectors.toMap(OrderProductItemCreateDTO::getItemId, item -> item.getQuantity()));

        Customer customer = customerService.getCustomerById(order.getCustomerId());

        Set<Long> drinkIds = order.getDrinks().stream().map((drink) -> drink.getItemId()).collect(Collectors.toSet());
        Set<Long> productIds = order.getProducts().stream().map((product) -> product.getItemId())
                .collect(Collectors.toSet());

        Map<Long, Product> dbProductsMap = productService.getActiveProductByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));
        Map<Long, Drink> dbDrinksMap = drinkService.getActiveDrinkByIds(drinkIds).stream()
                .collect(Collectors.toMap(Drink::getDrinkId, drink -> drink));

        Map<String, List<Long>> itemsNotFound = new HashMap<>();

        if (drinkIds.size() != dbDrinksMap.size()) {
            List<Long> drinkIsNotExisted = drinkIds.stream()
                    .filter(id -> dbDrinksMap.get(id) == null)
                    .collect(Collectors.toList());

            itemsNotFound.put("Drinks not found with ids", drinkIsNotExisted);
        }

        if (productIds.size() != dbProductsMap.size()) {
            List<Long> productIsNotExisted = productIds.stream()
                    .filter(id -> dbProductsMap.get(id) == null)
                    .collect(Collectors.toList());
            itemsNotFound.put("Products not found with ids", productIsNotExisted);
        }

        if (itemsNotFound.size() > 0) {
            throw new DataNotFoundException("Item not found", Arrays.asList(itemsNotFound));
        }

        // Check stock
        List<Long> drinkIsOutOfStock = new ArrayList<>();
        List<Long> productIsOutOfStock = new ArrayList<>();

        if (order.getDrinks() != null || order.getDrinks().size() > 0) {
            order.getDrinks().stream().forEach(drink -> {
                Long drinkQuantity = drink.getQuantity();
                Long stockQuantity = dbDrinksMap.get(drink.getItemId()).getStockQuantity();
                if (drinkQuantity > stockQuantity) {
                    drinkIsOutOfStock.add(drink.getItemId());
                }
            });
        }

        if (order.getProducts() != null || order.getProducts().size() > 0) {
            order.getProducts().stream().forEach(product -> {
                Long productQuantity = product.getQuantity();
                Long stockQuantity = dbProductsMap.get(product.getItemId()).getStockQuantity();
                if (productQuantity > stockQuantity) {
                    productIsOutOfStock.add(product.getItemId());
                }
            });
        }

        if (drinkIsOutOfStock.size() > 0 || productIsOutOfStock.size() > 0) {
            Map<String, List<Long>> data = new HashMap<>();
            data.put("drinkIsOutOfStock", drinkIsOutOfStock);
            data.put("productIsOutOfStock", productIsOutOfStock);
            throw new DataConflictException("Item is out of stock", Arrays.asList(data));
        }

        // Minus Stock

        // Minus Drink Stock
        if (dbDrinksMap.size() > 0) {
            dbDrinksMap.values().forEach(drink -> {
                Long quantity = orderDrinkQuantityMap.get(drink.getDrinkId());
                drink.setStockQuantity(drink.getStockQuantity() - quantity);
            });
            drinkService.saveDrinks(dbDrinksMap.values().stream().toList());
        }

        // Minus Product Stock
        if (dbProductsMap.size() > 0) {
            dbProductsMap.values().forEach(product -> {
                Long quantity = orderProductQuantityMap.get(product.getProductId());
                product.setStockQuantity(product.getStockQuantity() - quantity);
            });
            productService.saveProducts(dbProductsMap.values().stream().toList());
        }

        // Get Promotion
        List<PromotionItem> promotionItemsList = new ArrayList<>();

        if (dbDrinksMap.values().size() > 0) {
            dbDrinksMap.values().forEach(drink -> {
                List<PromotionItem> promotionItemsByDrink = drink.getPromotionItems();
                promotionItemsList.addAll(promotionItemsByDrink);
            });
        }

        if (dbProductsMap.values().size() > 0) {
            dbProductsMap.values().forEach(product -> {
                List<PromotionItem> promotionItemsByProduct = product.getPromotionItems();
                promotionItemsList.addAll(promotionItemsByProduct);
            });
        }
        // <promotionId, promotion>
        Map<Long, Promotion> promotionsMap = new HashMap<>();

        if (promotionItemsList.size() > 0) {
            List<Promotion> promotionList = promotionService.getActivePromotionsByIds(
                    promotionItemsList.stream().map(PromotionItem::getPromotionId).collect(Collectors.toList()));
            promotionsMap.putAll(promotionList.stream()
                    .collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion)));
        }
        // <productId, promotionId>
        Map<Long, Long> productPromotionIdMap = new HashMap<>();

        // <drinkId, promotionId>
        Map<Long, Long> drinkPromotionIdMap = new HashMap<>();

        // Pick Best Promotion for product and drink
        if (promotionsMap.values().size() > 0) {
            promotionItemsList.stream().forEach(promotionItem -> {

                // Get Best Promotion And Map To productPromotionIdMap
                if (promotionItem.getProductId() != null) {

                    if (productPromotionIdMap.get(promotionItem.getProductId()) != null) {
                        Promotion oldPromotion = promotionsMap
                                .get(productPromotionIdMap.get(promotionItem.getProductId()));
                        Promotion newPromotion = promotionsMap.get(promotionItem.getPromotionId());
                        Promotion bestPromotion = promotionService.pickBetterPromotion(oldPromotion, newPromotion);
                        if (bestPromotion != null)
                            productPromotionIdMap.put(promotionItem.getProductId(), bestPromotion.getPromotionId());
                    } else {
                        Promotion promotion = promotionsMap.get(promotionItem.getPromotionId());
                        if (promotion != null)
                            productPromotionIdMap.put(promotionItem.getProductId(), promotionItem.getPromotionId());
                    }
                }

                // Get Best Promotion And Map To drinkPromotionIdMap
                if (promotionItem.getDrinkId() != null) {

                    if (drinkPromotionIdMap.get(promotionItem.getDrinkId()) != null) {
                        Promotion oldPromotion = promotionsMap.get(drinkPromotionIdMap.get(promotionItem.getDrinkId()));
                        Promotion newPromotion = promotionsMap.get(promotionItem.getPromotionId());
                        Promotion bestPromotion = promotionService.pickBetterPromotion(oldPromotion, newPromotion);
                        if (bestPromotion != null)
                            drinkPromotionIdMap.put(promotionItem.getDrinkId(), bestPromotion.getPromotionId());
                    } else {
                        Promotion promotion = promotionsMap.get(promotionItem.getPromotionId());
                        if (promotion != null)
                            drinkPromotionIdMap.put(promotionItem.getDrinkId(), promotionItem.getPromotionId());
                    }
                }
            });
        }

        // Get Order Items
        List<OrderItem> orderItemsList = new ArrayList<>();

        if (dbDrinksMap.values().size() > 0) {
            orderDrinkQuantityMap.forEach((drinkId, quantity) -> {
                Drink drink = dbDrinksMap.get(drinkId);
                Promotion promotion = promotionsMap.get(drinkPromotionIdMap.get(drinkId));
                Double originalPrice = drink.getPrice() * quantity;
                Double discountAmount = promotion != null
                        ? promotionService.calculateTotalDiscountWithMaxDiscountQuantity(quantity,
                                promotion.getMaxApplicableQuantity(),
                                promotion.getDiscountAmount() != null ? promotion.getDiscountAmount()
                                        : promotion.getDiscountPercentage() * drink.getPrice())
                        : 0.0;
                Double finalPrice = originalPrice - discountAmount;

                OrderItem orderItem = new OrderItem();
                orderItem.setDrinkId(drink.getDrinkId());
                orderItem.setName(drink.getDrinkName());
                orderItem.setOriginalPrice(originalPrice);
                orderItem.setDiscountAmount(discountAmount);
                orderItem.setFinalPrice(finalPrice);
                orderItem.setQuantity(quantity);
                orderItem.setPromotionId(
                        promotion != null && promotion.getMaxApplicableQuantity() > 0 ? promotion.getPromotionId()
                                : null);
                orderItemsList.add(orderItem);

                if (promotion != null && promotion.getMaxApplicableQuantity() > 0) {

                    if (promotion.getMaxApplicableQuantity() > quantity)
                        promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() - quantity);
                    else
                        promotion.setMaxApplicableQuantity(0L);
                    promotionsMap.replace(promotion.getPromotionId(), promotion);
                }
            });
        }

        if (dbProductsMap.size() > 0) {

            orderProductQuantityMap.forEach((productId, quantity) -> {
                Product product = dbProductsMap.get(productId);
                Promotion promotion = promotionsMap.get(productPromotionIdMap.get(productId));
                Double originalPrice = product.getPrice() * quantity;
                Double discountAmount = promotion != null
                        ? promotionService.calculateTotalDiscountWithMaxDiscountQuantity(quantity,
                                promotion.getMaxApplicableQuantity(),
                                promotion.getDiscountAmount() != null ? promotion.getDiscountAmount()
                                        : promotion.getDiscountPercentage() * product.getPrice())
                        : 0.0;
                Double finalPrice = originalPrice - discountAmount;

                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.getProductId());
                orderItem.setName(product.getProductName());
                orderItem.setOriginalPrice(originalPrice);
                orderItem.setDiscountAmount(discountAmount);
                orderItem.setFinalPrice(finalPrice);
                orderItem.setQuantity(quantity);
                orderItem.setPromotionId(promotion != null ? promotion.getPromotionId() : null);
                orderItemsList.add(orderItem);

                if (promotion != null && promotion.getMaxApplicableQuantity() > 0) {

                    if (promotion.getMaxApplicableQuantity() > quantity)
                        promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() - quantity);
                    else
                        promotion.setMaxApplicableQuantity(0L);
                    promotionsMap.replace(promotion.getPromotionId(), promotion);
                }
            });

        }
        // Update MaxApplicableQuantity Promotions
        promotionService.updatePromotions(promotionsMap.values().stream().toList());

        // Calculate Total Amount

        Double totalAmount = orderItemsList.stream().mapToDouble(OrderItem::getFinalPrice).sum();

        Order newOrder = new Order();
        newOrder.setCustomerName(order.getCustomerName() != null ? order.getCustomerName() : customer.getFullName());
        newOrder.setCustomerPhone(
                order.getCustomerPhone() != null ? order.getCustomerPhone() : customer.getPhoneNumber());
        newOrder.setCustomerEmail(order.getCustomerEmail() != null ? order.getCustomerEmail() : customer.getEmail());
        newOrder.setCustomerAddress(
                order.getCustomerAddress() != null ? order.getCustomerAddress() : customer.getAddress());
        newOrder.setCustomerId(customer.getCustomerId());
        newOrder.setUserId(user != null ? user.getUserId() : null);
        newOrder.setOrderItems(orderItemsList);
        newOrder.setTotalAmount(totalAmount);
        newOrder.setStatus(OrderStatus.Pending);
        newOrder.setNotes(order.getNotes());

        Order savedOrder = orderRepo.save(newOrder);

        orderItemsList.forEach(item -> {
            item.setOrderId(savedOrder.getOrderId());
        });

        orderItemService.createOrderItems(orderItemsList);

        return savedOrder;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status, Long staffId) {
        // Pending, Processcing, Completed, Cancelled
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus().equals(OrderStatus.Completed) || order.getStatus().equals(OrderStatus.Cancelled)) {
            throw new DataConflictException("Cannot update status of order with status: " + order.getStatus());
        }

        if (status.equals(OrderStatus.Completed)) {
            order.setUserId(staffId);
        }

        order.setStatus(status);
        order.setUpdatedAt(new Date(System.currentTimeMillis()));

        // Save order
        orderRepo.save(order);

        if (!status.equals(OrderStatus.Cancelled)) {
            return;
        }

        // Plus Stock
        List<OrderItem> dbOrderItems = order.getOrderItems();

        // <drinkId, OrderItem>
        Map<Long, OrderItem> dbDrinkItems = dbOrderItems.stream().filter(item -> item.getDrinkId() != null)
                .collect(Collectors.toMap(OrderItem::getDrinkId, item -> item));

        // <productId, OrderItem>
        Map<Long, OrderItem> dbDroductItems = dbOrderItems.stream().filter(item -> item.getProductId() != null)
                .collect(Collectors.toMap(OrderItem::getProductId, item -> item));

        Set<Long> drinkIds = dbDrinkItems.keySet().stream().collect(Collectors.toSet());
        Set<Long> productIds = dbDroductItems.keySet().stream().collect(Collectors.toSet());

        List<Drink> dbDrinks = drinkService.getActiveDrinkByIds(drinkIds);
        List<Product> dbProducts = productService.getActiveProductByIds(productIds);

        // Plus Drink Stock
        if (dbDrinks.size() > 0) {
            dbDrinks.forEach(drink -> {
                Long returnQuantity = dbDrinkItems.get(drink.getDrinkId()).getQuantity();
                drink.setStockQuantity(drink.getStockQuantity() + returnQuantity);
            });
            drinkService.saveDrinks(dbDrinks);
        }

        // Plus Product Stock
        if (dbProducts.size() > 0) {
            dbProducts.forEach(product -> {
                Long quantity = dbDrinkItems.get(product.getProductId()).getQuantity();
                product.setStockQuantity(product.getStockQuantity() + quantity);
            });
            productService.saveProducts(dbProducts);
        }

        // Return MaxapplicableQuantity

        Map<Long, Promotion> promotionMap = promotionService
                .getActivePromotionsByIds(
                        dbOrderItems.stream().filter(orderItem -> orderItem.getPromotionId() != null)
                                .map(OrderItem::getPromotionId).toList(),
                        order.getCreatedAt())
                .stream().collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

        dbOrderItems.forEach(orderItem -> {
            if (orderItem.getPromotionId() != null) {
                Promotion promotion = promotionMap.get(orderItem.getPromotionId());
                promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() + orderItem.getQuantity());
                promotionMap.replace(orderItem.getPromotionId(), promotion);
            }
        });
        promotionService.updatePromotions(promotionMap.values().stream().toList());
    }

    @Transactional
    public Order updateOrder(Long orderId, OrderUpdateDTO order) {

        Order existingOrder = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        // 1.[Check order status] Start
        if (existingOrder.getStatus().equals(OrderStatus.Completed)
                || existingOrder.getStatus().equals(OrderStatus.Cancelled)) {
            throw new DataConflictException("Cannot update order with status: " + existingOrder.getStatus());
        }
        // 1.[Check order status] End

        // Get all new drinks and products id
        Map<Long, Long> newDrinkIdsAndQuantity = order.getDrinks().stream().collect(
                Collectors.toMap(OrderProductItemCreateDTO::getItemId, OrderProductItemCreateDTO::getQuantity));
        Map<Long, Long> newProductIdsAndQuantity = order.getProducts().stream().collect(
                Collectors.toMap(OrderProductItemCreateDTO::getItemId, OrderProductItemCreateDTO::getQuantity));

        // Get all stock drinks and products to check existed and stock
        List<Drink> newDrinks = drinkService
                .getActiveDrinkByIds(newDrinkIdsAndQuantity.keySet().stream().collect(Collectors.toSet()));
        List<Product> newProducts = productService
                .getActiveProductByIds(newProductIdsAndQuantity.keySet().stream().collect(Collectors.toSet()));

        // 2.[Check new products and drinks id is existed] Start

        Map<String, List<Long>> itemIsNotExisted = new HashMap<>();
        if (newDrinks.size() < order.getDrinks().size()) {
            itemIsNotExisted.put("drinks", newDrinkIdsAndQuantity.keySet().stream().filter(drinkId -> {
                return !newDrinks.stream().map(Drink::getDrinkId).collect(Collectors.toList()).contains(drinkId);
            }).toList());
        }

        if (newProducts.size() < order.getProducts().size()) {
            itemIsNotExisted.put("products", newProductIdsAndQuantity.keySet().stream().filter(productId -> {
                return !newProducts.stream().map(Product::getProductId).collect(Collectors.toList())
                        .contains(productId);
            }).toList());
        }

        if (itemIsNotExisted.size() > 0) {
            throw new DataNotFoundException("Item not found", List.of(itemIsNotExisted));
        }
        // 2.[Check new products and drinks id is existed] End

        // [Check new products and drinks are difference old products and drinks] Start
        List<OrderItem> oldOrderItems = existingOrder.getOrderItems();
        // [divide drink and product from orderItems to be easy to plus for stock] Start
        List<OrderItem> oldDrinkOrderItems = oldOrderItems.stream().filter(item -> item.getDrinkId() != null)
                .collect(Collectors.toList());
        List<OrderItem> oldProducOrdertItems = oldOrderItems.stream().filter(item -> item.getProductId() != null)
                .toList();
        // [divide drink and product from orderItems to be easy to plus for stock] End

        Map<Long, Long> oldDrinkIdsAndQuantity = oldDrinkOrderItems.stream()
                .collect(Collectors.toMap(OrderItem::getDrinkId, OrderItem::getQuantity));
        Map<Long, Long> oldProductIdsAndQuantity = oldProducOrdertItems.stream()
                .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));

        Boolean isDifferenceItems = true;

        if (oldDrinkIdsAndQuantity.size() == newDrinkIdsAndQuantity.size()
                && oldProductIdsAndQuantity.size() == newProductIdsAndQuantity.size()) {
            if (oldDrinkIdsAndQuantity.equals(newDrinkIdsAndQuantity)
                    && oldProductIdsAndQuantity.equals(newProductIdsAndQuantity)) {
                isDifferenceItems = false;
            }
        }

        List<OrderItem> newOrderItems = new ArrayList<>();

        // [Check new products and drinks are difference old products and drinks] End

        // If new products and drinks are difference old products and drinks then return
        // quantity to old products and drinks
        // Check stock new products and drinks
        // Delete old orderItems
        // then minus quantity to new products and drinks
        if (isDifferenceItems) {
            // 3.[get orderItems to return the quantity of Product or Drink] Start
            // [get drink and product from id] Start
            List<Drink> oldDrinks = drinkService
                    .getActiveDrinkByIds(oldDrinkIdsAndQuantity.keySet().stream().collect(Collectors.toSet()));
            List<Product> oldProducts = productService
                    .getActiveProductByIds(oldProductIdsAndQuantity.keySet().stream().collect(Collectors.toSet()));
            // [get drink and product from id] End

            // [Plus Drink Stock, Product Stock and save to database] Start
            if (oldDrinks.size() > 0) {
                oldDrinks.forEach(drink -> {
                    Long quantity = oldDrinkIdsAndQuantity.get(drink.getDrinkId());
                    drink.setStockQuantity(drink.getStockQuantity() + quantity);
                });

                drinkService.saveDrinks(oldDrinks);
            }

            if (oldProducts.size() > 0) {
                oldProducts.forEach(product -> {
                    Long quantity = oldProductIdsAndQuantity.get(product.getProductId());
                    product.setStockQuantity(product.getStockQuantity() + quantity);
                });
                productService.saveProducts(oldProducts);
            }
            // [Plus Drink Stock, Product Stock and save to database] End
            // 3.[get orderItems to return the quantity of Product or Drink] End

            // 4.[Delete order items existed] Start
            orderItemService.deleteOrderItemsByOrderId(orderId);
            // 4.[Delete order items existed] End
            // 5.[Check order new item is out of stock] Start
            Map<String, List<Long>> itemIsOutOfStock = new HashMap<>();

            if (newDrinks.size() > 0) {

                List<Long> drinkIsOutOfStockId = newDrinks.stream().filter(drink -> {
                    Long quantity = newDrinkIdsAndQuantity.get(drink.getDrinkId());
                    return drink.getStockQuantity() < quantity;
                }).map(Drink::getDrinkId).collect(Collectors.toList());

                if (drinkIsOutOfStockId.size() > 0) {
                    itemIsOutOfStock.put("drinks", drinkIsOutOfStockId);
                }
            }

            if (newProducts.size() > 0) {
                List<Long> productIsOutOfStockId = newProducts.stream().filter(product -> {
                    Long quantity = newProductIdsAndQuantity.get(product.getProductId());
                    return product.getStockQuantity() < quantity;
                }).map(Product::getProductId).collect(Collectors.toList());
                if (productIsOutOfStockId.size() > 0) {
                    itemIsOutOfStock.put("products", productIsOutOfStockId);
                }
            }

            if (itemIsOutOfStock.size() > 0) {
                throw new DataNotFoundException("Item out of stock", List.of(itemIsOutOfStock));
            }

            // 5.[Check order new item is out of stock] End

            // 6. [Minus stock for new item] Start

            if (newDrinks.size() > 0) {
                newDrinks.forEach(drink -> {
                    Long quantity = newDrinkIdsAndQuantity.get(drink.getDrinkId());
                    drink.setStockQuantity(drink.getStockQuantity() - quantity);
                });
                drinkService.saveDrinks(newDrinks);
            }

            if (newProducts.size() > 0) {
                newProducts.forEach(product -> {
                    Long quantity = newProductIdsAndQuantity.get(product.getProductId());
                    product.setStockQuantity(product.getStockQuantity() - quantity);
                });
                productService.saveProducts(newProducts);
            }
            // 6. [Minus stock for new Item] End

            // 7. [Create new OrderItems] Start

            List<PromotionItem> promotionItems = new ArrayList<>();

            promotionItems.addAll(newDrinks.stream().flatMap(drink -> drink.getPromotionItems().stream()).toList());
            promotionItems
                    .addAll(newProducts.stream().flatMap(product -> product.getPromotionItems().stream()).toList());

            List<Long> promotionIds = promotionItems.stream().map(PromotionItem::getPromotionId)
                    .collect(Collectors.toList());

            Map<Long, Promotion> activePromotions = promotionService.getActivePromotionsByIds(promotionIds)
                    .stream()
                    .collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

            // <drinkId, PromotionItem>
            Map<Long, PromotionItem> activeDrinkItems = promotionItems.stream()
                    .filter(promotionItem -> (promotionItem.getDrinkId() != null
                            && activePromotions.get(promotionItem.getPromotionId()) != null))
                    .collect(Collectors.toMap(PromotionItem::getDrinkId, promotionItem -> promotionItem));

            // <productId, PromotionItem>
            Map<Long, PromotionItem> activeProductItems = promotionItems.stream()
                    .filter(promotionItem -> (promotionItem.getProductId() != null
                            && activePromotions.get(promotionItem.getPromotionId()) != null))
                    .collect(Collectors.toMap(PromotionItem::getProductId, promotionItem -> promotionItem));

            newDrinks.forEach(drink -> {
                Long quantity = newDrinkIdsAndQuantity.get(drink.getDrinkId());

                PromotionItem promotionItem = activeDrinkItems.get(drink.getDrinkId());
                Double originalPrice = drink.getPrice() * quantity;
                Double discountAmount = 0.0;
                Double finalPrice = originalPrice;
                if (promotionItem != null) {
                    Promotion promotion = activePromotions.get(promotionItem.getPromotionId());
                    if (promotion.getDiscountAmount() != null) {
                        discountAmount = promotion.getDiscountAmount() * quantity;
                    } else if (promotion.getDiscountPercentage() != null) {
                        discountAmount = (originalPrice * promotion.getDiscountPercentage()) / 100.0;
                    }
                    finalPrice = originalPrice - discountAmount;
                }
                OrderItem newOrderItem = OrderItem.builder()
                        .drinkId(drink.getDrinkId())
                        .quantity(quantity)
                        .orderId(orderId)
                        .name(drink.getDrinkName())
                        .originalPrice(originalPrice)
                        .discountAmount(discountAmount)
                        .finalPrice(finalPrice)
                        .build();
                newOrderItems.add(newOrderItem);
            });

            newProducts.forEach(product -> {
                Long quantity = newProductIdsAndQuantity.get(product.getProductId());

                PromotionItem promotionItem = activeProductItems.get(product.getProductId());
                Double originalPrice = product.getPrice() * quantity;
                Double discountAmount = 0.0;
                Double finalPrice = originalPrice;
                if (promotionItem != null) {
                    Promotion promotion = activePromotions.get(promotionItem.getPromotionId());
                    if (promotion.getDiscountAmount() != null) {
                        discountAmount = promotion.getDiscountAmount() * quantity;
                    } else if (promotion.getDiscountPercentage() != null) {
                        discountAmount = (originalPrice * promotion.getDiscountPercentage()) / 100.0;
                    }
                    finalPrice = originalPrice - discountAmount;
                }

                OrderItem newOrderItem = OrderItem.builder()
                        .productId(product.getProductId())
                        .quantity(quantity)
                        .orderId(orderId)
                        .name(product.getProductName())
                        .originalPrice(originalPrice)
                        .discountAmount(discountAmount)
                        .finalPrice(finalPrice)
                        .build();
                newOrderItems.add(newOrderItem);
            });

            orderItemService.createOrderItems(newOrderItems);
            // 7. [Create new OrderItems] End
        }

        // 8.[Update order status and updateTime] Start

        if (order.getCustomerId() != existingOrder.getCustomer().getCustomerId()) {
            Customer newCustomer = customerService.getCustomerById(order.getCustomerId());
            existingOrder.setCustomer(newCustomer);
            existingOrder.setCustomerName(newCustomer.getFullName());
            existingOrder.setCustomerEmail(newCustomer.getEmail());
            existingOrder.setCustomerPhone(newCustomer.getPhoneNumber());
        }

        existingOrder.setNotes(order.getNotes());
        existingOrder.setUpdatedAt(new Date(System.currentTimeMillis()));
        Order savedOrder = orderRepo.save(existingOrder);
        if (isDifferenceItems) {
            savedOrder.setOrderItems(newOrderItems);
        }
        // 8.[Update order status and updateTime] End

        return savedOrder;
    }

    @Transactional
    public void cancelOrder(Long orderId, Long ownerId, Boolean isAdmin) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        // 1.[Check order status] START
        if (order.getStatus().equals(OrderStatus.Cancelled) || order.getStatus().equals(OrderStatus.Completed)) {
            throw new DataConflictException("Cannot update status of order with status: " + order.getStatus());
        }
        // 1.[Check order status] END

        // 2.[Check order owner or staff] START
        if (!isAdmin) {
            if (!order.getCustomer().getId().equals(ownerId)) {
                throw new AccessDeniedException("You don't have permission to cancel this order");
            }
        }
        // 2.[Check order owner or staff] END

        // 3. [Return quantity to stock] START

        List<OrderItem> orderItems = order.getOrderItems();

        Set<Long> drinkIds = orderItems.stream().filter(item -> item.getDrinkId() != null).map(OrderItem::getDrinkId)
                .collect(Collectors.toSet());
        Set<Long> productIds = orderItems.stream().filter(item -> item.getProductId() != null)
                .map(OrderItem::getProductId).collect(Collectors.toSet());

        List<Drink> drinks = drinkService.getActiveDrinkByIds(drinkIds);
        List<Product> products = productService.getActiveProductByIds(productIds);

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
        order.setStatus(OrderStatus.Cancelled);
        order.setUpdatedAt(new Date(System.currentTimeMillis()));
        orderRepo.save(order);
        // 4.[Update order status and updateTime] END

        // 5.[Return MaxApplicableQuantity Promotions] Start

        Map<Long, Promotion> promotionMap = promotionService
                .getActivePromotionsByIds(
                        orderItems.stream().filter(orderItem -> orderItem.getPromotionId() != null)
                                .map(OrderItem::getPromotionId).toList(),
                        order.getCreatedAt())
                .stream().collect(Collectors.toMap(Promotion::getPromotionId, promotion -> promotion));

        orderItems.forEach(orderItem -> {
            if (orderItem.getPromotionId() != null) {
                Promotion promotion = promotionMap.get(orderItem.getPromotionId());
                promotion.setMaxApplicableQuantity(promotion.getMaxApplicableQuantity() + orderItem.getQuantity());
                promotionMap.replace(orderItem.getPromotionId(), promotion);
            }
        });
        promotionService.updatePromotions(promotionMap.values().stream().toList());

        // 5.[Return MaxApplicableQuantity Promotions] End

    }
}
