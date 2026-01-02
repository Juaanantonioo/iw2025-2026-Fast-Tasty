package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.*;

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

    // Imagen del producto
    @Lob
    private byte[] image;

    // Relación con FoodType
    @ManyToOne
    @JoinColumn(name = "food_type_id")
    private FoodType type;

    // Relación con alérgenos
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_allergen",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    private Set<Allergen> allergens = new HashSet<>();

    @Column(nullable = false)
    private int stock = 0;

    // =========================
    // INGREDIENTES
    // =========================
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_ingredient",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "ingredient_name")),
            @AttributeOverride(name = "quantity", column = @Column(name = "quantity"))
    })
    private List<Ingredient> ingredients = new ArrayList<>();

    // =========================
    // CLASE EMBEDDABLE
    // =========================
    @Embeddable
    public static class Ingredient {

        @Column(nullable = false)
        private String name;

        @Column(nullable = false)
        private double quantity;

        public Ingredient() {}

        public Ingredient(String name, double quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Ingredient)) return false;
            Ingredient that = (Ingredient) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================
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

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
}
