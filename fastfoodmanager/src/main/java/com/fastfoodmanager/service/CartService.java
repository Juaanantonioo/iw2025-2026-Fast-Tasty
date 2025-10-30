package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Product;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final List<Product> cartItems = new ArrayList<>();

    public void addProduct(Product product) {
        cartItems.add(product);
    }

    public void removeProduct(Product product) {
        cartItems.remove(product);
    }

    public List<Product> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public BigDecimal getTotalPrice() {
        return cartItems.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}