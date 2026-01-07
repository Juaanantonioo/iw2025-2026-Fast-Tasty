package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 800)
    private String description;

    private Double price;

    private boolean active = true;

    @Lob
    private byte[] image;

    // ============================
    // Cantidades por categoría
    // ============================
    @Column(nullable = false)
    private int mainQuantity = 1;

    @Column(nullable = false)
    private int sideQuantity = 1;

    @Column(nullable = false)
    private int drinkQuantity = 1;

    @Column(nullable = false)
    private int secondaryQuantity = 0;

    @Column(nullable = false)
    private int dessertQuantity = 0;

    // ============================
    // Productos por categoría
    // ============================
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "menu_main_products",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> mainProducts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "menu_secondary_products",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> secondaryProducts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "menu_drink_products",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> drinkProducts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "menu_side_products",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> sideProducts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "menu_dessert_products",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> dessertProducts = new ArrayList<>();

    // ============================
    // CONSTRUCTOR VACÍO (JPA)
    // ============================
    public Menu() {}

    // ============================================================
    // 🔥 CONSTRUCTOR DESDE MenuSnapshot (para CartItem y ofertas)
    // ============================================================
    public Menu(MenuSnapshot snapshot) {
        this.name = snapshot.getName();
        this.description = snapshot.getDescription();
        this.price = snapshot.getPrice();
        this.image = snapshot.getImage();

        this.mainQuantity = snapshot.getMainQuantity();
        this.sideQuantity = snapshot.getSideQuantity();
        this.drinkQuantity = snapshot.getDrinkQuantity();
        this.secondaryQuantity = snapshot.getSecondaryQuantity();
        this.dessertQuantity = snapshot.getDessertQuantity();

        // No reconstruimos listas de productos porque no son necesarias
        // para aplicar ofertas ni para calcular el precio del menú.
    }

    // ============================
    // GETTERS Y SETTERS
    // ============================
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public int getMainQuantity() { return mainQuantity; }
    public void setMainQuantity(int mainQuantity) { this.mainQuantity = mainQuantity; }

    public int getSideQuantity() { return sideQuantity; }
    public void setSideQuantity(int sideQuantity) { this.sideQuantity = sideQuantity; }

    public int getDrinkQuantity() { return drinkQuantity; }
    public void setDrinkQuantity(int drinkQuantity) { this.drinkQuantity = drinkQuantity; }

    public int getSecondaryQuantity() { return secondaryQuantity; }
    public void setSecondaryQuantity(int secondaryQuantity) { this.secondaryQuantity = secondaryQuantity; }

    public int getDessertQuantity() { return dessertQuantity; }
    public void setDessertQuantity(int dessertQuantity) { this.dessertQuantity = dessertQuantity; }

    public List<Product> getMainProducts() { return mainProducts; }
    public void setMainProducts(List<Product> mainProducts) { this.mainProducts = mainProducts; }

    public List<Product> getSecondaryProducts() { return secondaryProducts; }
    public void setSecondaryProducts(List<Product> secondaryProducts) { this.secondaryProducts = secondaryProducts; }

    public List<Product> getDrinkProducts() { return drinkProducts; }
    public void setDrinkProducts(List<Product> drinkProducts) { this.drinkProducts = drinkProducts; }

    public List<Product> getSideProducts() { return sideProducts; }
    public void setSideProducts(List<Product> sideProducts) { this.sideProducts = sideProducts; }

    public List<Product> getDessertProducts() { return dessertProducts; }
    public void setDessertProducts(List<Product> dessertProducts) { this.dessertProducts = dessertProducts; }
}
