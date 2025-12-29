package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto asociado
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Pedido al que pertenece este item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // Cantidad pedida
    @Column(nullable = false)
    private int quantity;

    // Precio unitario en el momento del pedido (por si cambia en el futuro)
    @Column(nullable = false)
    private double unitPrice;

    public OrderItem() {
    }

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = (product != null && product.getPrice() != null) ? product.getPrice() : 0.0;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    // --- Métodos de utilidad ---

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    @Override
    public String toString() {
        String name = (product != null && product.getName() != null) ? product.getName() : "Producto";
        return name + " x" + quantity + " (" + unitPrice + "€)";
    }
}