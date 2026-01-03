package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto embebido (snapshot)
    @Embedded
    private ProductSnapshot product;

    // Pedido al que pertenece este item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // Cantidad pedida
    @Column(nullable = false)
    private int quantity;

    // Precio unitario en el momento del pedido
    @Column(nullable = false)
    private double unitPrice;

    public OrderItem() {}

    // Constructor para snapshot
    public OrderItem(ProductSnapshot snapshot, int quantity) {
        this.product = snapshot;
        this.quantity = quantity;
        this.unitPrice = snapshot.getPrice();
    }

    // Constructor para Product real (lo convierte a snapshot)
    public OrderItem(Product product, int quantity) {
        this.product = new ProductSnapshot(product);
        this.quantity = quantity;
        this.unitPrice = product != null ? product.getPrice() : 0.0;
    }

    public Long getId() { return id; }

    public ProductSnapshot getProduct() { return product; }

    // Setter CORRECTO
    public void setProduct(ProductSnapshot snapshot) {
        this.product = snapshot;
        this.unitPrice = snapshot.getPrice();
    }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    @Override
    public String toString() {
        String name = (product != null && product.getName() != null) ? product.getName() : "Producto";
        return name + " x" + quantity + " (" + unitPrice + "€)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return quantity == that.quantity &&
                Double.compare(that.unitPrice, unitPrice) == 0 &&
                Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity, unitPrice);
    }
}
