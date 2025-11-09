package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cliente que realiza el pedido
    @ManyToOne(optional = false)
    private User customer;

    // Estado del pedido (EN COCINA, PREPARANDO, LISTO, ENTREGADO, PAGADO, etc.)
    @Column(nullable = false)
    private String status = "EN COCINA";

    // Operario asignado (opcional)
    private String assignedTo;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Total del pedido
    private Double total = 0.0;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(User customer, List<OrderItem> items) {
        this.customer = customer;
        if (items != null) {
            this.items = items;
            this.items.forEach(i -> i.setOrder(this)); // enlace bidireccional
        }
        recalcTotal();
    }

    /**
     * Recalcula el total del pedido sumando el precio de cada item.
     */
    public void recalcTotal() {
        this.total = items.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        this.items.forEach(i -> i.setOrder(this)); // mantener referencia
        recalcTotal();
    }
}
