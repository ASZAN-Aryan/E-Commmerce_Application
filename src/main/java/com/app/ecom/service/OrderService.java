
package com.app.ecom.service;

import com.app.ecom.dto.OrderItemResponse;
import com.app.ecom.dto.OrderResponse;
import com.app.ecom.model.*;
import com.app.ecom.repository.CartItemRepository;
import com.app.ecom.repository.OrderRepository;
import com.app.ecom.repository.ProductRepository;
import com.app.ecom.repository.UserRepositry;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepositry userRepositry;

    // Place order from cart
    @Transactional
    public OrderResponse placeOrder(Long userId) {

        User user = userRepositry.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        List<CartItem> cartItems =
                cartItemRepository.findByUser_Id(userId);

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = productRepository
                    .findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            int quantity = cartItem.getQuantity();

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        product.getName() + " is inactive");
            }

            if (product.getStockQuantity() == null ||
                    quantity > product.getStockQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + product.getName());
            }

            // Use the price stored in the cart
            BigDecimal price = cartItem.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(price);

            order.getItems().add(orderItem);

            BigDecimal subtotal = price.multiply(
                    BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(subtotal);

            // Reduce product stock
            product.setStockQuantity(
                    product.getStockQuantity() - quantity);

            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful checkout
        cartItemRepository.deleteByUser_Id(userId);

        return mapToOrderResponse(savedOrder);
    }

    // Get all orders of a user
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {

        if (!userRepositry.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found");
        }

        return orderRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    // Get a particular order
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {

        Order order = orderRepository
                .findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        return mapToOrderResponse(order);
    }

    // Cancel order
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {

        Order order = orderRepository
                .findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order cannot be cancelled");
        }

        // Restore product stock
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity());

            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        return mapToOrderResponse(orderRepository.save(order));
    }

    // Convert Order entity to DTO
    private OrderResponse mapToOrderResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());

        response.setItems(
                order.getItems()
                        .stream()
                        .map(this::mapToOrderItemResponse)
                        .toList()
        );

        return response;
    }

    // Convert OrderItem entity to DTO
    private OrderItemResponse mapToOrderItemResponse(
            OrderItem item) {

        OrderItemResponse response = new OrderItemResponse();

        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setImageUrl(item.getProduct().getImageUrl());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());

        response.setSubtotal(
                item.getPrice().multiply(
                        BigDecimal.valueOf(item.getQuantity())));

        return response;
    }
    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId, OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        order.setStatus(status);

        return mapToOrderResponse(orderRepository.save(order));
    }
}
