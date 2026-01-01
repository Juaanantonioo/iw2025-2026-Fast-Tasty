package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {

    private List<OrderItem> items = new ArrayList<>();

    public void addProduct(Product product) {
        if (product == null) return;

        // Buscar si el producto ya existe en el carrito
        for (OrderItem item : items) {
            if (item.getProduct() != null && item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        // Si no existe, crear nuevo item
        items.add(new OrderItem(product, 1));
    }

    public void add(Product product) {
        addProduct(product);
    }

    public void decrement(OrderItem item) {
        if (item == null || item.getProduct() == null) return;

        for (OrderItem cartItem : items) {
            if (cartItem.getProduct() != null &&
                cartItem.getProduct().getId().equals(item.getProduct().getId())) {
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                } else {
                    items.remove(cartItem);
                }
                return;
            }
        }
    }

    public void remove(OrderItem item) {
        if (item == null || item.getProduct() == null) return;

        items.removeIf(cartItem ->
            cartItem.getProduct() != null &&
            cartItem.getProduct().getId().equals(item.getProduct().getId())
        );
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public List<OrderItem> getItemsCopy() {
        List<OrderItem> copy = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItem newItem = new OrderItem(item.getProduct(), item.getQuantity());
            newItem.setUnitPrice(item.getUnitPrice());
            copy.add(newItem);
        }
        return copy;
    }

    public double total() {
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    public int countItems() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public void clear() {
        items.clear();
    }
}