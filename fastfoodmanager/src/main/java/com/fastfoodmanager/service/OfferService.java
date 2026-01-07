package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.*;
import com.fastfoodmanager.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OfferService {

    private final OfferRepository offerRepo;

    public OfferService(OfferRepository offerRepo) {
        this.offerRepo = offerRepo;
    }

    // ============================================================
    // CRUD BÁSICO PARA ADMINOFFERSVIEW
    // ============================================================

    public Offer save(Offer offer) {
        return offerRepo.save(offer);
    }

    public void delete(Offer offer) {
        if (offer != null && offer.getId() != null) {
            offerRepo.delete(offer);
        }
    }

    public List<Offer> findAll() {
        return offerRepo.findAll();
    }

    // ============================================================
    // OFERTAS ACTIVAS
    // ============================================================

    public List<Offer> getValidOffers() {
        return offerRepo.findValidOffers(LocalDateTime.now());
    }

    // ============================================================
    // OFERTAS ZxY ACTIVAS (para CartaView)
    // ============================================================

    public List<Offer> findZxYOffers() {
        return offerRepo.findAll().stream()
                .filter(Offer::isActive)
                .filter(o -> o.getMode() == OfferMode.ZxY)
                .toList();
    }

    // ============================================================
    // APLICAR TODAS LAS OFERTAS A LOS ITEMS DEL CARRITO
    // ============================================================

    public double applyAllOffers(List<CartItem> items) {
        double totalDiscount = 0;

        for (Offer offer : getValidOffers()) {
            totalDiscount += applyOffer(offer, items);
        }

        return totalDiscount;
    }

    // ============================================================
    // APLICAR UNA OFERTA INDIVIDUAL
    // ============================================================

    public double applyOffer(Offer offer, List<CartItem> items) {
        return switch (offer.getMode()) {
            case DISCOUNT -> applyDiscount(offer, items);
            case ZxY -> applyZxY(offer, items);
        };
    }

    // ============================================================
    // PRECIO FINAL DE PRODUCTO / MENÚ
    // ============================================================

    public double getFinalPriceForProduct(Product product) {
        CartItem item = new CartItem(product, 1);
        double discount = applyAllOffers(List.of(item));
        return Math.max(product.getPrice() - discount, 0);
    }

    public double getFinalPriceForMenu(Menu menu) {
        CartItem item = new CartItem(menu, 1);
        double discount = applyAllOffers(List.of(item));
        return Math.max(menu.getPrice() - discount, 0);
    }

    // ============================================================
    // DESCUENTO (%)
    // ============================================================

    private double applyDiscount(Offer offer, List<CartItem> items) {

        if (offer.getDiscountPercentage() == null || offer.getDiscountPercentage() <= 0)
            return 0;

        double discount = 0;

        for (CartItem item : items) {

            if (!isItemAffected(offer, item))
                continue;

            double base = item.getUnitPrice() * item.getQuantity();
            discount += base * (offer.getDiscountPercentage() / 100.0);
        }

        return discount;
    }

    // ============================================================
    // ZxY (ej: 2x1, 3x2, 5x3)
    // ============================================================

    private double applyZxY(Offer offer, List<CartItem> items) {

        if (offer.getZValue() == null || offer.getYValue() == null)
            return 0;

        int Z = offer.getZValue();
        int Y = offer.getYValue();

        if (Z <= 1 || Y < 1 || Y > Z)
            return 0;

        // 1. Obtener todos los precios de los productos afectados
        List<Double> prices = items.stream()
                .filter(i -> isItemAffected(offer, i))
                .flatMap(i ->
                        Collections.nCopies(i.getQuantity(), i.getUnitPrice()).stream()
                )
                .sorted(Comparator.reverseOrder()) // mayor → menor
                .collect(Collectors.toList());

        if (prices.size() < Z)
            return 0;

        double discount = 0;

        // 2. Procesar en grupos de Z
        for (int i = 0; i + Z <= prices.size(); i += Z) {

            List<Double> group = prices.subList(i, i + Z);

            // Los Z-Y más baratos son gratis
            List<Double> free = group.stream()
                    .sorted() // menor → mayor
                    .limit(Z - Y)
                    .toList();

            discount += free.stream().mapToDouble(Double::doubleValue).sum();
        }

        return discount;
    }

    // ============================================================
    // COMPROBAR SI UN ITEM ESTÁ AFECTADO POR LA OFERTA
    // ============================================================

    public boolean isItemAffected(Offer offer, CartItem item) {

        Product p = item.getProduct();

        return switch (offer.getTargetType()) {

            case GLOBAL -> true;

            case PRODUCT -> offer.getProducts().stream()
                    .anyMatch(prod -> prod.getName().equalsIgnoreCase(p.getName()));

            case CATEGORY -> offer.getCategories().stream()
                    .anyMatch(cat -> cat.getName().equalsIgnoreCase(p.getType().getName()));
        };
    }

    // ============================================================
    // PRIORIDADES EN OFERTAS
    // ============================================================

    private int getPriority(OfferTarget target) {
        return switch (target) {
            case GLOBAL -> 3;
            case CATEGORY -> 2;
            case PRODUCT -> 1;
        };
    }

    // ============================================================
    // DETECTAR CONFLICTOS ENTRE OFERTAS
    // ============================================================

    public List<String> findConflicts(Offer newOffer) {

        List<String> conflicts = new ArrayList<>();

        for (Offer existing : getValidOffers()) {

            if (existing.getId().equals(newOffer.getId()))
                continue;

            int newP = getPriority(newOffer.getTargetType());
            int oldP = getPriority(existing.getTargetType());

            // GLOBAL existente bloquea todo
            if (existing.getTargetType() == OfferTarget.GLOBAL &&
                    newOffer.getTargetType() != OfferTarget.GLOBAL) {

                conflicts.add("Ya existe una oferta GLOBAL activa: " + existing.getName());
                continue;
            }

            // GLOBAL nueva siempre permitida
            if (newOffer.getTargetType() == OfferTarget.GLOBAL) {
                continue;
            }

            // Si la nueva tiene mayor prioridad → permitido
            if (newP > oldP) {
                continue;
            }

            // Revisar solapamientos
            if (existing.getTargetType() == OfferTarget.CATEGORY &&
                    newOffer.getTargetType() == OfferTarget.CATEGORY) {

                for (FoodType cat : newOffer.getCategories()) {
                    if (existing.getCategories().contains(cat)) {
                        conflicts.add("La categoría " + cat.getName() +
                                " ya está en oferta: " + existing.getName());
                    }
                }
            }

            if (existing.getTargetType() == OfferTarget.PRODUCT &&
                    newOffer.getTargetType() == OfferTarget.PRODUCT) {

                for (Product p : newOffer.getProducts()) {
                    if (existing.getProducts().contains(p)) {
                        conflicts.add("El producto " + p.getName() +
                                " ya está en oferta: " + existing.getName());
                    }
                }
            }

            if (existing.getTargetType() == OfferTarget.CATEGORY &&
                    newOffer.getTargetType() == OfferTarget.PRODUCT) {

                for (Product p : newOffer.getProducts()) {
                    if (existing.getCategories().contains(p.getType())) {
                        conflicts.add("El producto " + p.getName() +
                                " ya está afectado por la oferta de categoría " + existing.getName());
                    }
                }
            }

            if (existing.getTargetType() == OfferTarget.PRODUCT &&
                    newOffer.getTargetType() == OfferTarget.CATEGORY) {

                for (FoodType cat : newOffer.getCategories()) {
                    for (Product p : existing.getProducts()) {
                        if (p.getType().equals(cat)) {
                            conflicts.add("La categoría " + cat.getName() +
                                    " ya tiene productos en oferta individual (" + existing.getName() + ")");
                        }
                    }
                }
            }
        }

        return conflicts;
    }

    // ============================================================
    // LIMPIAR OFERTAS DE MENOR PRIORIDAD
    // ============================================================

    public void removeLowerPriorityAssignments(Offer newOffer) {

        for (Offer existing : getValidOffers()) {

            if (existing.getId().equals(newOffer.getId()))
                continue;

            int newP = getPriority(newOffer.getTargetType());
            int oldP = getPriority(existing.getTargetType());

            if (newP > oldP) {

                switch (newOffer.getTargetType()) {

                    case GLOBAL -> {
                        existing.setProducts(null);
                        existing.setCategories(null);
                    }

                    case CATEGORY -> {
                        for (FoodType cat : newOffer.getCategories()) {
                            if (existing.getTargetType() == OfferTarget.PRODUCT) {
                                existing.getProducts().removeIf(p -> p.getType().equals(cat));
                            }
                        }
                    }

                    case PRODUCT -> {
                        if (existing.getTargetType() == OfferTarget.PRODUCT) {
                            existing.getProducts().removeAll(newOffer.getProducts());
                        }
                    }
                }

                offerRepo.save(existing);
            }
        }
    }
}
