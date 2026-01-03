package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
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
     * Compara dos productos para saber si son EXACTAMENTE iguales,
     * incluyendo ingredientes personalizados.
     */
    private boolean areProductsEqual(Product p1, Product p2) {
        if (p1 == null || p2 == null) return false;

        // Comparar campos básicos
        if (!Objects.equals(p1.getName(), p2.getName())) return false;
        if (!Objects.equals(p1.getPrice(), p2.getPrice())) return false;
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

        for (OrderItem item : items) {
            if (areProductsEqual(item.getProduct(), product)) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        // Si no existe un item igual, añadir uno nuevo
        items.add(new OrderItem(product, 1));
    }

    public void add(Product product) {
        addProduct(product);
    }

    public void decrement(OrderItem item) {
        if (item == null || item.getProduct() == null) return;

        for (OrderItem cartItem : items) {
            if (areProductsEqual(cartItem.getProduct(), item.getProduct())) {
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
                areProductsEqual(cartItem.getProduct(), item.getProduct())
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
