package com.fastfoodmanager.domain;

import java.util.ArrayList;
import java.util.List;

public class ProductSnapshot {

    private String name;
    private String description;
    private double price;
    private byte[] image;
    private FoodType type;

    private List<Product.Ingredient> ingredients = new ArrayList<>();

    public ProductSnapshot() {}

    // Constructor desde Product
    public ProductSnapshot(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.image = product.getImage();
        this.type = product.getType();

        this.ingredients = product.getIngredients() != null
                ? new ArrayList<>(product.getIngredients())
                : new ArrayList<>();
    }

    // Constructor desde datos reconstruidos (OrderItem @PostLoad)
    public ProductSnapshot(String name, double price, List<Product.Ingredient> ingredients) {
        this.name = name;
        this.price = price;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    // Constructor desde otro snapshot
    public ProductSnapshot(ProductSnapshot snapshot) {
        this.name = snapshot.getName();
        this.description = snapshot.getDescription();
        this.price = snapshot.getPrice();
        this.image = snapshot.getImage();
        this.ingredients = new ArrayList<>(snapshot.getIngredients());
    }

    // GETTERS
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public byte[] getImage() { return image; }
    public List<Product.Ingredient> getIngredients() { return ingredients; }
    public FoodType getType() { return type; }

    // SETTERS
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImage(byte[] image) { this.image = image; }
    public void setIngredients(List<Product.Ingredient> ingredients) { this.ingredients = ingredients; }
    public void setType(FoodType type) { this.type = type; }

    // Convertir snapshot → Product editable
    public Product toProduct() {
        Product p = new Product();
        p.setName(this.name);
        p.setDescription(this.description);
        p.setPrice(this.price);
        p.setImage(this.image);
        p.setType(this.type);


        List<Product.Ingredient> rebuilt = new ArrayList<>();
        for (var ing : this.ingredients) {
            Product.Ingredient newIng = new Product.Ingredient();
            newIng.setName(ing.getName());
            newIng.setQuantity(ing.getQuantity());
            newIng.setCustomizable(ing.isCustomizable());
            rebuilt.add(newIng);
        }

        p.setIngredients(rebuilt);
        return p;
    }
}
