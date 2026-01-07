package com.fastfoodmanager.domain;

public class CartItem {

    private Product product;   // Producto real
    private Menu menu;         // Menú real

    private int quantity;
    private double unitPrice;

    // ============================================================
    // CONSTRUCTORES
    // ============================================================

    // Producto normal
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    // Menú normal
    public CartItem(Menu menu, int quantity) {
        this.menu = menu;
        this.quantity = quantity;
        this.unitPrice = menu.getPrice();
    }

    // Desde OrderItem → para OrderService y OfferService
    public CartItem(OrderItem item) {
        this.product = item.getProduct() != null ? item.getProduct().toProduct() : null;
        this.menu = item.getMenu() != null ? new Menu(item.getMenu()) : null; // 🔥 reconstrucción correcta
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public Product getProduct() {
        return product;
    }

    public Menu getMenu() {
        return menu;
    }

    public boolean isProduct() {
        return product != null;
    }

    public boolean isMenu() {
        return menu != null;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    // ============================================================
    // SETTERS
    // ============================================================

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    public double getTotal() {
        return unitPrice * quantity;
    }

    // Para ofertas: obtener FoodType real (solo productos)
    public FoodType getFoodType() {
        if (isProduct()) return product.getType();
        return null; // 🔥 los menús NO tienen categoría
    }

    // Para ofertas: obtener ID real
    public Long getBaseId() {
        if (isProduct()) return product.getId();
        if (isMenu()) return menu.getId();
        return null;
    }
}
