package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    public enum Status {
        ENVIADO_A_COCINA,   // el operador aún no lo tocó
        PREPARANDO,
        LISTO,
        EN_REPARTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private Double total;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ENVIADO_A_COCINA;

    // usuario asignado (username del operario)
    private String assignedTo;

    public Order() {}

    public Order(String customerName, Double total, Status status, String assignedTo) {
        this.customerName = customerName;
        this.total = total;
        this.status = status;
        this.assignedTo = assignedTo;
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}
