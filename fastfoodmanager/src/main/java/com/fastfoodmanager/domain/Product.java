package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private boolean active = true;

    // Nueva relación con FoodType
    @ManyToOne
    @JoinColumn(name = "food_type_id")
    private FoodType type;

    // Nueva relación con Allergen
    @ManyToMany
    @JoinTable(
            name = "product_allergen",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    private List<Allergen> allergens;

    @Column(nullable = false)
    private int stock = 0;

    public Product() {}

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

    // Nuevo getter/setter FoodType
    public FoodType getType() { return type; }
    public void setType(FoodType type) { this.type = type; }

    // Nuevo getter/setter Allergen (lista)
    public List<Allergen> getAllergens() { return allergens; }
    public void setAllergens(List<Allergen> allergens) { this.allergens = allergens; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
