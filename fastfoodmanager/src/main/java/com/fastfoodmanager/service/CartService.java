package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.*;

@Service
@SessionScope
public class CartService {

    private final Map<Long, OrderItem> items = new HashMap<>();

    /** Obtiene todos los items del carrito */
    public List<OrderItem> getItems() {
        return new ArrayList<>(items.values());
    }

    /** Obtiene una copia de los items para crear pedido */
    public List<OrderItem> getItemsCopy() {
        return new ArrayList<>(items.values());
    }

    /** Añade un producto al carrito (incrementa cantidad si ya existe) */
    public void add(Product product) {
        OrderItem existing = items.get(product.getId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            OrderItem newItem = new OrderItem();
            newItem.setProduct(product);
            newItem.setUnitPrice(product.getPrice());
            newItem.setQuantity(1);
            items.put(product.getId(), newItem);
        }
    }

    /** Alias para mantener compatibilidad con CartaView */
    public void addProduct(Product product) {
        add(product);
    }

    /** Decrementa la cantidad de un item en 1 */
    public void decrement(OrderItem item) {
        Product product = item.getProduct();
        if (product != null) {
            OrderItem existing = items.get(product.getId());
            if (existing != null) {
                if (existing.getQuantity() > 1) {
                    existing.setQuantity(existing.getQuantity() - 1);
                } else {
                    // Si la cantidad es 1, eliminar completamente
                    items.remove(product.getId());
                }
            }
        }
    }

    /** Elimina completamente un item del carrito */
    public void remove(OrderItem item) {
        Product product = item.getProduct();
        if (product != null) {
            items.remove(product.getId());
        }
    }

    /** Alias para mantener compatibilidad con CartaView (si lo usa) */
    public void removeProduct(Product product) {
        OrderItem item = items.get(product.getId());
        if (item != null) {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                items.remove(product.getId());
            }
        }
    }

    /** Calcula el total del carrito */
    public double total() {
        return items.values().stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    /** Cuenta el número total de unidades en el carrito */
    public int countItems() {
        return items.values().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /** Vacía el carrito */
    public void clear() {
        items.clear();
    }

    /** Método alternativo para compatibilidad */
    public BigDecimal getTotalPrice() {
        return BigDecimal.valueOf(total());
    }

    /** Método alternativo para compatibilidad */
    public List<Product> getCartItems() {
        List<Product> products = new ArrayList<>();
        for (OrderItem item : items.values()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                products.add(item.getProduct());
            }
        }
        return products;
    }

    /** Método alternativo para compatibilidad */
    public int getItemCount() {
        return countItems();
    }

    /** Método alternativo para compatibilidad */
    public void clearCart() {
        clear();
    }
}