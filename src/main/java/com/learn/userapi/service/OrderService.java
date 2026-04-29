package com.learn.userapi.service;

import com.learn.userapi.dto.request.OrderCreateRequest;
import com.learn.userapi.dto.response.OrderResponse;
import com.learn.userapi.exception.ResourceNotFoundException;
import com.learn.userapi.model.Order;
import com.learn.userapi.model.OrderStatus;
import com.learn.userapi.model.User;
import com.learn.userapi.repository.OrderRepository;
import com.learn.userapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
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
}