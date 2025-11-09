package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos del producto
    private String name;
    private String description;
    private Double price;
    private boolean active = true;
    private String allergens;

    @Column(nullable = false)
    private int stock = 0; // <-- NUEVO: cantidad disponible en inventario

    // ---- Getters y Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public int getStock() { return stock; }       // <-- NUEVO
    public void setStock(int stock) { this.stock = stock; } // <-- NUEVO
}
