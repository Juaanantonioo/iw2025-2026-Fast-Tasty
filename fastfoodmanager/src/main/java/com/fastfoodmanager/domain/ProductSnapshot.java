package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.*;

@Embeddable
public class ProductSnapshot {

    private String name;
    private String description;
    private double price;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_item_ingredient", joinColumns = @JoinColumn(name = "order_item_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "ingredient_name")),
            @AttributeOverride(name = "quantity", column = @Column(name = "quantity")),
            @AttributeOverride(name = "customizable", column = @Column(name = "customizable"))
    })
    private List<Product.Ingredient> ingredients = new ArrayList<>();

    public ProductSnapshot() {}

    // Constructor desde Product (original)
    public ProductSnapshot(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.ingredients = product.getIngredients() != null
                ? new ArrayList<>(product.getIngredients())
                : new ArrayList<>();
    }

    // 🔥 Constructor desde ProductSnapshot (NECESARIO)
    public ProductSnapshot(ProductSnapshot snapshot) {
        this.name = snapshot.getName();
        this.description = snapshot.getDescription();
        this.price = snapshot.getPrice();
        this.ingredients = new ArrayList<>(snapshot.getIngredients());
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public List<Product.Ingredient> getIngredients() { return ingredients; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setIngredients(List<Product.Ingredient> ingredients) { this.ingredients = ingredients; }
}
