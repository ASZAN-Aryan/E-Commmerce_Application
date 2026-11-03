
package com.app.ecom.controller;

import com.app.ecom.dto.CartItemRequest;
import com.app.ecom.dto.CartResponse;
import com.app.ecom.service.CartService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody CartItemRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(userId, request));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("X-User-ID") Long userId) {

        return ResponseEntity.ok(
                cartService.getCart(userId));
    }

    @PatchMapping("/items/{itemId}/increase")
    public ResponseEntity<CartResponse> increaseQuantity(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                cartService.increaseQuantity(userId, itemId));
    }

    @PatchMapping("/items/{itemId}/decrease")
    public ResponseEntity<CartResponse> decreaseQuantity(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                cartService.decreaseQuantity(userId, itemId));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(
                cartService.removeItem(userId, itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-ID") Long userId) {

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
