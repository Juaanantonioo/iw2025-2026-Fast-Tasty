package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//atributos de los productos
    private String name;
    private String description;
    private Double price;
    private boolean active = true;
    private String allergens;
    //funciones de los productos
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAllergens() { return allergens; }

    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public void setAllergens(String allergens) { this.allergens = allergens; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
