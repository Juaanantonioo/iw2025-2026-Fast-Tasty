package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@VaadinSessionScope
public class CartService {

    private final List<OrderItem> items = new ArrayList<>();

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<OrderItem> getItemsCopy() {
        // Copia para crear el pedido (no devolvemos la lista interna)
        return new ArrayList<>(items);
    }

    public void add(Product p) {
        if (p == null || p.getId() == null) return;

        for (OrderItem it : items) {
            if (it.getProduct() != null && p.getId().equals(it.getProduct().getId())) {
                it.setQuantity(it.getQuantity() + 1);
                return;
            }
        }
        items.add(new OrderItem(p, 1));
    }

    public void remove(OrderItem item) {
        items.remove(item);
    }

    public void decrement(OrderItem item) {
        if (item == null) return;
        int q = item.getQuantity();
        if (q <= 1) {
            remove(item);
        } else {
            item.setQuantity(q - 1);
        }
    }

    public void clear() {
        items.clear();
    }

    public double total() {
        return items.stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
    }

    public int countItems() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}
