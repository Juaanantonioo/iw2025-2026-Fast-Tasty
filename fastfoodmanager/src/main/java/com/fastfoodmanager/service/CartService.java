package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@SessionScope
public class CartService {

    private final OfferService offerService;

    public CartService(OfferService offerService) {
        this.offerService = offerService;
    }

    private List<OrderItem> items = new ArrayList<>();

    // ============================
    // COMPARAR PRODUCTOS
    // ============================
    private boolean areSnapshotsEqual(ProductSnapshot p1, ProductSnapshot p2) {
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

    // ============================
    // COMPARAR MENÚS
    // ============================
    private boolean areMenuSnapshotsEqual(MenuSnapshot m1, MenuSnapshot m2) {
        if (m1 == null || m2 == null) return false;

        if (!Objects.equals(m1.getName(), m2.getName())) return false;
        if (m1.getPrice() != m2.getPrice()) return false;
        if (!Objects.equals(m1.getDescription(), m2.getDescription())) return false;

        if (m1.getMainQuantity() != m2.getMainQuantity()) return false;
        if (m1.getSideQuantity() != m2.getSideQuantity()) return false;
        if (m1.getDrinkQuantity() != m2.getDrinkQuantity()) return false;
        if (m1.getSecondaryQuantity() != m2.getSecondaryQuantity()) return false;
        if (m1.getDessertQuantity() != m2.getDessertQuantity()) return false;

        if (!m1.getMainProducts().equals(m2.getMainProducts())) return false;
        if (!m1.getSideProducts().equals(m2.getSideProducts())) return false;
        if (!m1.getDrinkProducts().equals(m2.getDrinkProducts())) return false;
        if (!m1.getSecondaryProducts().equals(m2.getSecondaryProducts())) return false;
        if (!m1.getDessertProducts().equals(m2.getDessertProducts())) return false;

        return true;
    }

    // ============================
    // AÑADIR PRODUCTO
    // ============================
    public void addProduct(Product product) {
        if (product == null) return;

        ProductSnapshot snapshot = new ProductSnapshot(product);

        // Si ya existe el mismo snapshot → incrementar
        for (OrderItem item : items) {
            if (item.getProduct() != null &&
                    areSnapshotsEqual(item.getProduct(), snapshot)) {

                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        // Precio final con ofertas
        double finalPrice = offerService.getFinalPriceForProduct(product);

        OrderItem newItem = new OrderItem(snapshot, 1);
        newItem.setUnitPrice(finalPrice);

        items.add(newItem);
        applyZxYUnitPrices();
    }

    // ============================
    // AÑADIR MENÚ
    // ============================
    public void addMenu(Menu menu) {
        if (menu == null) return;

        MenuSnapshot snapshot = new MenuSnapshot(menu);

        for (OrderItem item : items) {
            if (item.getMenu() != null &&
                    areMenuSnapshotsEqual(item.getMenu(), snapshot)) {

                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        double finalPrice = offerService.getFinalPriceForMenu(menu);

        OrderItem newItem = new OrderItem(snapshot, 1);
        newItem.setUnitPrice(finalPrice);

        items.add(newItem);
        applyZxYUnitPrices();
    }

    // ============================
    // GET ITEMS
    // ============================
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    // ============================
    // TOTAL SIN OFERTAS (LEGACY)
    // ============================
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

    // ============================
    // DECREMENTAR
    // ============================
    public void decrement(OrderItem item) {
        if (item == null) return;

        for (OrderItem cartItem : items) {

            if (cartItem.getProduct() != null && item.getProduct() != null &&
                    areSnapshotsEqual(cartItem.getProduct(), item.getProduct())) {

                if (cartItem.getQuantity() > 1) cartItem.setQuantity(cartItem.getQuantity() - 1);
                else items.remove(cartItem);
                applyZxYUnitPrices();
                return;
            }

            if (cartItem.getMenu() != null && item.getMenu() != null &&
                    areMenuSnapshotsEqual(cartItem.getMenu(), item.getMenu())) {

                if (cartItem.getQuantity() > 1) cartItem.setQuantity(cartItem.getQuantity() - 1);
                else items.remove(cartItem);
                applyZxYUnitPrices();
                return;
            }
        }
    }

    // ============================
    // SNAPSHOTS
    // ============================
    public void addProductSnapshot(ProductSnapshot snapshot) {
        if (snapshot == null) return;

        for (OrderItem item : items) {
            if (item.getProduct() != null &&
                    areSnapshotsEqual(item.getProduct(), snapshot)) {

                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        // Precio final con ofertas
        double finalPrice = offerService.getFinalPriceForProduct(snapshot.toProduct());

        OrderItem newItem = new OrderItem(snapshot, 1);
        newItem.setUnitPrice(finalPrice);

        items.add(newItem);
        applyZxYUnitPrices();
    }

    public void addMenuSnapshot(MenuSnapshot snapshot) {

        double finalPrice = snapshot.getPrice(); // ya tiene el precio final

        OrderItem newItem = new OrderItem(snapshot, 1);
        newItem.setUnitPrice(finalPrice);

        items.add(newItem);
    }

    public void remove(OrderItem item) {
        if (item == null) return;

        items.removeIf(cartItem -> {
            if (cartItem.getProduct() != null && item.getProduct() != null)
                return areSnapshotsEqual(cartItem.getProduct(), item.getProduct());

            if (cartItem.getMenu() != null && item.getMenu() != null)
                return areMenuSnapshotsEqual(cartItem.getMenu(), item.getMenu());

            return false;
        });

        applyZxYUnitPrices();
    }

    public List<OrderItem> getItemsCopy() {
        List<OrderItem> copy = new ArrayList<>();

        for (OrderItem item : items) {

            if (item.getProduct() != null) {
                OrderItem newItem = new OrderItem(
                        new ProductSnapshot(item.getProduct()),
                        item.getQuantity()
                );
                newItem.setUnitPrice(item.getUnitPrice());
                copy.add(newItem);
            }

            if (item.getMenu() != null) {
                OrderItem newItem = new OrderItem(
                        new MenuSnapshot(item.getMenu()),
                        item.getQuantity()
                );
                newItem.setUnitPrice(item.getUnitPrice());
                copy.add(newItem);
            }
        }

        return copy;
    }

    public void clear() {
        items.clear();
    }

    // ============================================================
    // OFERTAS: SUBTOTAL, DESCUENTO Y TOTAL FINAL
    // ============================================================

    public double calculateSubtotal() {
        return items.stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
    }

    public double calculateDiscount() {
        List<CartItem> cartItems = items.stream()
                .map(CartItem::new)
                .toList();

        return offerService.applyAllOffers(cartItems);
    }

    public double calculateTotal() {
        return Math.max(calculateSubtotal() - calculateDiscount(), 0);
    }

    private void applyZxYUnitPrices() {

        for (Offer offer : offerService.findZxYOffers()) {

            int Z = offer.getZValue();
            int Y = offer.getYValue();

            // 1. Obtener items afectados según el tipo de oferta
            List<OrderItem> affected = items.stream()
                    .filter(i -> i.getProduct() != null)
                    .filter(i -> {

                        ProductSnapshot snap = i.getProduct();

                        return switch (offer.getTargetType()) {

                            case GLOBAL -> true;

                            case PRODUCT -> offer.getProducts() != null &&
                                    offer.getProducts().stream()
                                            .anyMatch(prod ->
                                                    prod.getName().equalsIgnoreCase(snap.getName())
                                            );

                            case CATEGORY -> offer.getCategories() != null &&
                                    snap.getType() != null &&
                                    offer.getCategories().stream()
                                            .anyMatch(cat ->
                                                    cat.getName().equalsIgnoreCase(snap.getType().getName())
                                            );
                        };
                    })
                    .toList();

            if (affected.size() < Z)
                continue;

            // 2. Expandir items según cantidad
            List<OrderItem> expanded = new ArrayList<>();
            for (OrderItem item : affected) {
                for (int i = 0; i < item.getQuantity(); i++) {
                    expanded.add(item);
                }
            }

            // 3. Ordenar por precio descendente
            expanded.sort((a, b) -> Double.compare(b.getUnitPrice(), a.getUnitPrice()));

            // 4. Aplicar regla ZxY
            for (int i = 0; i + Z <= expanded.size(); i += Z) {

                List<OrderItem> group = expanded.subList(i, i + Z);

                // Los Y más caros se pagan
                List<OrderItem> pay = group.stream()
                        .sorted((a, b) -> Double.compare(b.getUnitPrice(), a.getUnitPrice()))
                        .limit(Y)
                        .toList();

                // Los demás son gratis
                List<OrderItem> free = group.stream()
                        .filter(o -> !pay.contains(o))
                        .toList();

                free.forEach(o -> o.setUnitPrice(0));
            }
        }
    }
}