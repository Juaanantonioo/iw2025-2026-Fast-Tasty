package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import com.vaadin.flow.server.VaadinSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class CartSession implements Serializable {

    private static final String CART_KEY = "CART_ITEMS";

    private CartSession() {}

    @SuppressWarnings("unchecked")
    public static List<OrderItem> getCart() {
        Object obj = VaadinSession.getCurrent().getAttribute(CART_KEY);
        if (obj instanceof List<?>) {
            return (List<OrderItem>) obj;
        }
        List<OrderItem> empty = new ArrayList<>();
        VaadinSession.getCurrent().setAttribute(CART_KEY, empty);
        return empty;
    }

    public static void clear() {
        VaadinSession.getCurrent().setAttribute(CART_KEY, new ArrayList<OrderItem>());
    }

    public static int countUnits() {
        return getCart().stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public static void addProduct(Product p) {
        if (p == null) return;

        List<OrderItem> cart = getCart();

        OrderItem existing = cart.stream()
                .filter(i -> i.getProduct() != null && i.getProduct().getId() != null
                        && i.getProduct().getId().equals(p.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            cart.add(new OrderItem(p, 1));
        }

        VaadinSession.getCurrent().setAttribute(CART_KEY, cart);
    }

    public static void removeItem(OrderItem item) {
        if (item == null) return;
        List<OrderItem> cart = getCart();
        cart.remove(item);
        VaadinSession.getCurrent().setAttribute(CART_KEY, cart);
    }
}
