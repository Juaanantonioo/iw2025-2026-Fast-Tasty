package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto asociado
    @ManyToOne(optional = false)
    private Product product;

    // Pedido al que pertenece este item
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Cantidad pedida
    private int quantity;

    // Precio unitario en el momento del pedido (por si cambia en el futuro)
    private double unitPrice;

    public OrderItem() {}

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    // --- Métodos de utilidad ---

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    @Override
    public String toString() {
        return product.getName() + " x" + quantity + " (" + unitPrice + "€)";
    }
}
