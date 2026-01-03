package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.ProductSnapshot;
import com.vaadin.flow.server.VaadinSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /**
     * Compara dos snapshots para saber si representan el mismo producto personalizado.
     */
    private static boolean areSnapshotsEqual(ProductSnapshot p1, ProductSnapshot p2) {
        if (p1 == null || p2 == null) return false;

        if (!Objects.equals(p1.getName(), p2.getName())) return false;
        if (p1.getPrice() != p2.getPrice()) return false;
        if (!Objects.equals(p1.getDescription(), p2.getDescription())) return false;

        if (p1.getIngredients().size() != p2.getIngredients().size()) return false;

        for (int i = 0; i < p1.getIngredients().size(); i++) {
            var ing1 = p1.getIngredients().get(i);
            var ing2 = p2.getIngredients().get(i);

            if (!Objects.equals(ing1.getName(), ing2.getName())) return false;
            if (ing1.getQuantity() != ing2.getQuantity()) return false;
            if (ing1.isCustomizable() != ing2.isCustomizable()) return false;
        }

        return true;
    }

    /**
     * Añade un producto al carrito usando snapshot.
     */
    public static void addProduct(Product p) {
        if (p == null) return;

        List<OrderItem> cart = getCart();

        // Crear snapshot del producto
        ProductSnapshot snapshot = new ProductSnapshot(p);

        // Buscar si ya existe un item igual
        OrderItem existing = cart.stream()
                .filter(i -> i.getProduct() != null && areSnapshotsEqual(i.getProduct(), snapshot))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            // OrderItem(Product) ya crea el snapshot internamente
            cart.add(new OrderItem(p, 1));
        }

        VaadinSession.getCurrent().setAttribute(CART_KEY, cart);
    }

    public static void removeItem(OrderItem item) {
        if (item == null) return;

        List<OrderItem> cart = getCart();

        cart.removeIf(i -> areSnapshotsEqual(i.getProduct(), item.getProduct()));

        VaadinSession.getCurrent().setAttribute(CART_KEY, cart);
    }
}
