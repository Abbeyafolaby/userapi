package com.learn.userapi.service;

import com.learn.userapi.dto.request.OrderCreateRequest;
import com.learn.userapi.dto.response.OrderResponse;
import com.learn.userapi.exception.ResourceNotFoundException;
import com.learn.userapi.model.Order;
import com.learn.userapi.model.OrderStatus;
import com.learn.userapi.model.User;
import com.learn.userapi.repository.OrderRepository;
import com.learn.userapi.repository.UserRepository;
import com.learn.userapi.dto.request.AddOrderItemRequest;
import com.learn.userapi.dto.response.OrderItemResponse;
import com.learn.userapi.model.OrderItem;
import com.learn.userapi.model.Product;
import com.learn.userapi.repository.OrderItemRepository;
import com.learn.userapi.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.learn.userapi.dto.response.PagedResponse;
import com.learn.userapi.util.PageableValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.util.List;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final  ProductRepository productRepository;
    private static final Set<String> ALLOWED_ORDER_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "totalAmount", "status");

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        log.debug("Fetching orders for user id: {}", userId);
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderResponse::fromOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // verify this order actually belongs to this user
        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return OrderResponse.fromOrder(order);
    }

    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        log.debug("Creating order for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Order order = new Order(user);
        order.setTotalAmount(request.getTotalAmount());

        // use the helper method — keeps both sides of the relationship in sync
        user.addOrder(order);

        OrderResponse created = OrderResponse.fromOrder(orderRepository.save(order));
        log.info("Order created with id: {} for user id: {}", created.getId(), userId);
        return created;
    }

    public OrderResponse updateOrderStatus(Long userId, Long orderId,
                                           OrderStatus newStatus) {
        log.debug("Updating status for order id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }

        order.setStatus(newStatus);
        // dirty checking — Hibernate detects the change and issues UPDATE automatically
        log.info("Order id: {} status updated to: {}", orderId, newStatus);
        return OrderResponse.fromOrder(order);
    }

    public void deleteOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }

        orderRepository.delete(order);
        log.info("Order id: {} deleted", orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItems(Long userId, Long orderId) {
        // verify order belongs to user
        getOrderById(userId, orderId);
        return orderItemRepository.findByOrderId(orderId)
                .stream()
                .map(OrderItemResponse::fromOrderItem)
                .toList();
    }

    public OrderItemResponse addItemToOrder(Long userId, Long orderId,
                                            AddOrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }

        // prevent adding items to a completed or canceled order
        if (order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot add items to an order with status: " + order.getStatus());
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", request.getProductId()));

        // check if this product is already in the order — update quantity instead
        OrderItem item = orderItemRepository
                .findByOrderIdAndProductId(orderId, request.getProductId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                    return existing;
                })
                .orElseGet(() -> {
                    OrderItem newItem = new OrderItem(order, product, request.getQuantity());
                    order.addOrderItem(newItem);
                    return newItem;
                });

        OrderItem saved = orderItemRepository.save(item);

        // recalculate and persist the order total
        order.recalculateTotal();
        orderRepository.save(order);

        log.info("Item added to order id: {} product id: {} qty: {}",
                orderId, request.getProductId(), request.getQuantity());
        return OrderItemResponse.fromOrderItem(saved);
    }

    public void removeItemFromOrder(Long userId, Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", itemId));

        order.removeOrderItem(item);
        orderItemRepository.delete(item);

        order.recalculateTotal();
        orderRepository.save(order);

        log.info("Item id: {} removed from order id: {}", itemId, orderId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrdersByUserIdPaged(
            Long userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Pageable validated = PageableValidator.validate(pageable, ALLOWED_ORDER_SORT_FIELDS);

        Page<OrderResponse> page = orderRepository
                .findByUserId(userId, validated)
                .map(OrderResponse::fromOrder);

        log.info("Retrieved page {} of orders for user id: {}",
                page.getNumber(), userId);

        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByDateRange(Long userId,
                                                    LocalDateTime from,
                                                    LocalDateTime to) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return orderRepository.findByUserIdAndDateRange(userId, from, to)
                .stream()
                .map(OrderResponse::fromOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersAboveAmount(Long userId,
                                                    BigDecimal minAmount) {
        List<OrderStatus> excluded = List.of(OrderStatus.CANCELLED);
        return orderRepository.findByUserIdAndMinAmount(userId, minAmount, excluded)
                .stream()
                .map(OrderResponse::fromOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrdersFiltered(Long userId,
                                                          OrderStatus status,
                                                          Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        Pageable validated = PageableValidator.validate(pageable, ALLOWED_ORDER_SORT_FIELDS);
        Page<OrderResponse> page = orderRepository
                .findByUserIdFilteredPaged(userId, status, validated)
                .map(OrderResponse::fromOrder);
        return PagedResponse.from(page);
    }
}