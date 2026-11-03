
package com.app.ecom.service;

import com.app.ecom.dto.CartItemRequest;
import com.app.ecom.dto.CartItemResponse;
import com.app.ecom.dto.CartResponse;
import com.app.ecom.model.CartItem;
import com.app.ecom.model.Product;
import com.app.ecom.model.User;
import com.app.ecom.repository.CartItemRepository;
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
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepositry userRepositry;

    @Transactional
    public CartResponse addToCart(
            Long userId, CartItemRequest request) {

        if (request.getProductId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID is required");
        }

        int quantity = request.getQuantity() == null
                ? 1 : request.getQuantity();

        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        User user = userRepositry.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Product is inactive");
        }

        CartItem item = cartItemRepository
                .findByUser_IdAndProduct_Id(
                        userId, product.getId())
                .orElse(null);

        int newQuantity = quantity;

        if (item != null) {
            newQuantity += item.getQuantity();
        } else {
            item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setPrice(product.getPrice());
        }

        checkStock(product, newQuantity);

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCart(userId);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {

        if (!userRepositry.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found");
        }

        List<CartItem> items =
                cartItemRepository.findByUser_Id(userId);

        List<CartItemResponse> responses = items.stream()
                .map(this::mapItem)
                .toList();

        CartResponse response = new CartResponse();
        response.setUserId(userId);
        response.setItems(responses);
        response.setTotalItems(
                items.stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum());
        response.setTotalPrice(
                responses.stream()
                        .map(CartItemResponse::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        return response;
    }

    @Transactional
    public CartResponse increaseQuantity(
            Long userId, Long itemId) {

        CartItem item = getItem(userId, itemId);
        int quantity = item.getQuantity() + 1;

        checkStock(item.getProduct(), quantity);

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return getCart(userId);
    }

    @Transactional
    public CartResponse decreaseQuantity(
            Long userId, Long itemId) {

        CartItem item = getItem(userId, itemId);

        if (item.getQuantity() <= 1) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        }

        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(
            Long userId, Long itemId) {

        CartItem item = getItem(userId, itemId);
        cartItemRepository.delete(item);

        return getCart(userId);
    }

    @Transactional
    public void clearCart(Long userId) {

        if (!userRepositry.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found");
        }

        cartItemRepository.deleteByUser_Id(userId);
    }

    private CartItem getItem(Long userId, Long itemId) {

        return cartItemRepository
                .findByIdAndUser_Id(itemId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found for this user"));
    }

    private void checkStock(Product product, int quantity) {

        if (product.getStockQuantity() == null
                || quantity > product.getStockQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested quantity exceeds available stock");
        }
    }

    private CartItemResponse mapItem(CartItem item) {

        CartItemResponse response = new CartItemResponse();

        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setImageUrl(item.getProduct().getImageUrl());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(
                item.getPrice().multiply(
                        BigDecimal.valueOf(item.getQuantity())));

        return response;
    }
}
