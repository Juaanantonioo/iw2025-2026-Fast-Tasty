package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double price;
    private boolean active = true;

    // Imagen del producto como blob
    @Lob
    private byte[] image;

    // Relación con FoodType
    @ManyToOne
    @JoinColumn(name = "food_type_id")
    private FoodType type;

    // Relación con Allergen
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_allergen",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    private Set<Allergen> allergens;

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

    public FoodType getType() { return type; }
    public void setType(FoodType type) { this.type = type; }

    public Set<Allergen> getAllergens() { return allergens; }
    public void setAllergens(Set<Allergen> allergens) { this.allergens = allergens; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}
