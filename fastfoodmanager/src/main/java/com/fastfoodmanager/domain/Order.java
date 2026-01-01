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

    // Tipo del pedido: recoger en local o domicilio
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType = OrderType.PICKUP;
    // Enviar email de confirmación
    @Column(name = "send_email", nullable = false)
    private boolean sendEmail = false;

    // Dirección para domicilio (opcional)
    @Column(name = "delivery_address")
    private String deliveryAddress;

    // Cliente que realiza el pedido
    @ManyToOne(optional = false)
    private User customer;

    // Flujo: ENVIADO -> EN COCINA -> LISTO -> [EN REPARTO -> ENTREGADO] o [RECOGIDO]
    @Column(nullable = false)
    private String status = "ENVIADO";

    // Operario asignado (username)
    private String assignedTo;

    // Repartidor asignado (username)
    private String deliveryTo;

    // Pago simulado
    @Column(nullable = false)
    private boolean paid = false;

    private LocalDateTime paidAt;

    // Cocina
    @Column(nullable = false)
    private boolean cookedDone = false;

    private String cookedBy;
    private LocalDateTime cookedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Fecha de recogida (para pedidos PICKUP)
    private LocalDateTime pickedUpAt;

    private Double total = 0.0;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(User customer, List<OrderItem> items) {
        this.customer = customer;
        if (items != null) {
            this.items = items;
            this.items.forEach(i -> i.setOrder(this));
        }
        recalcTotal();
    }
    // Recalcula el total del pedido
    public void recalcTotal() {
        if (this.items == null || this.items.isEmpty()) {
            this.total = 0.0;
            return;
        }
        this.total = this.items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    public Long getId() { return id; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getDeliveryTo() { return deliveryTo; }
    public void setDeliveryTo(String deliveryTo) { this.deliveryTo = deliveryTo; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public boolean isCookedDone() { return cookedDone; }
    public void setCookedDone(boolean cookedDone) { this.cookedDone = cookedDone; }

    public String getCookedBy() { return cookedBy; }
    public void setCookedBy(String cookedBy) { this.cookedBy = cookedBy; }

    public LocalDateTime getCookedAt() { return cookedAt; }
    public void setCookedAt(LocalDateTime cookedAt) { this.cookedAt = cookedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items = (items != null) ? items : new ArrayList<>();
        this.items.forEach(i -> i.setOrder(this));
        recalcTotal();
    }

}