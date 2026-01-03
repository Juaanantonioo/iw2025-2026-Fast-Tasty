package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.ProductSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@SessionScope
public class CartService {

    private List<OrderItem> items = new ArrayList<>();

    /**
     * Compara dos snapshots para saber si son EXACTAMENTE iguales,
     * incluyendo ingredientes personalizados.
     */
    private boolean areSnapshotsEqual(ProductSnapshot p1, ProductSnapshot p2) {
        if (p1 == null || p2 == null) return false;

        // Comparar campos básicos
        if (!Objects.equals(p1.getName(), p2.getName())) return false;
        if (p1.getPrice() != p2.getPrice()) return false;
        if (!Objects.equals(p1.getDescription(), p2.getDescription())) return false;

        // Comparar ingredientes
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
     * Añade un producto al carrito. Si ya existe uno EXACTAMENTE igual,
     * aumenta la cantidad. Si no, crea un nuevo OrderItem.
     */
    public void addProduct(Product product) {
        if (product == null) return;

        // Crear snapshot del producto
        ProductSnapshot snapshot = new ProductSnapshot(product);

        for (OrderItem item : items) {
            if (areSnapshotsEqual(item.getProduct(), snapshot)) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        // Si no existe un item igual, añadir uno nuevo
        items.add(new OrderItem(product, 1)); // OrderItem ya crea el snapshot internamente
    }

    public void add(Product product) {
        addProduct(product);
    }

    public void addProductSnapshot(ProductSnapshot snapshot) {
        if (snapshot == null) return;

        for (OrderItem item : items) {
            if (areSnapshotsEqual(item.getProduct(), snapshot)) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        items.add(new OrderItem(snapshot, 1));
    }

    public void decrement(OrderItem item) {
        if (item == null || item.getProduct() == null) return;

        for (OrderItem cartItem : items) {
            if (areSnapshotsEqual(cartItem.getProduct(), item.getProduct())) {
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
                areSnapshotsEqual(cartItem.getProduct(), item.getProduct())
        );
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Devuelve una copia profunda del carrito,
     * necesaria para crear el pedido sin modificar el carrito original.
     */
    public List<OrderItem> getItemsCopy() {
        List<OrderItem> copy = new ArrayList<>();
        for (OrderItem item : items) {
            // item.getProduct() ahora es ProductSnapshot
            OrderItem newItem = new OrderItem(
                    new ProductSnapshot(item.getProduct()),
                    item.getQuantity()
            );
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
