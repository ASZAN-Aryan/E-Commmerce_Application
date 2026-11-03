
package com.app.ecom.controller;

import com.app.ecom.dto.OrderResponse;
import com.app.ecom.model.OrderStatus;
import com.app.ecom.service.OrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Checkout
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader("X-User-ID") Long userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(userId));
    }

    // Get all user orders
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @RequestHeader("X-User-ID") Long userId) {

        return ResponseEntity.ok(
                orderService.getUserOrders(userId));
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrder(userId, orderId));
    }

    // Cancel order
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.cancelOrder(userId, orderId));
    }
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, status));
    }
}
