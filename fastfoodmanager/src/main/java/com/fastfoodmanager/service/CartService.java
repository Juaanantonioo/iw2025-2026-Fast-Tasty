package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CartService {

    private final List<Product> cartItems = new ArrayList<>();

    /** Añade 1 unidad del producto al carrito. */
    public void addProduct(Product product) {
        cartItems.add(product);
    }

    /** Elimina 1 unidad del producto del carrito (si existe). */
    public void removeProduct(Product product) {
        cartItems.remove(product);
    }

    /** Devuelve una copia inmutable de la lista de items. */
    public List<Product> getCartItems() {
        return Collections.unmodifiableList(new ArrayList<>(cartItems));
    }

    /** Vacía el carrito. */
    public void clearCart() {
        cartItems.clear();
    }

    /** Número total de items (con repeticiones). */
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * Total del carrito en BigDecimal.
     * Como Product#getPrice() devuelve Double, convertimos a BigDecimal con valueOf.
     */
    public BigDecimal getTotalPrice() {
        return cartItems.stream()
                .map(p -> BigDecimal.valueOf(p.getPrice())) // Double -> BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
