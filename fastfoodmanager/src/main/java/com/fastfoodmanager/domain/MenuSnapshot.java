package com.fastfoodmanager.domain;

import java.util.ArrayList;
import java.util.List;

public class MenuSnapshot {

    private String name;
    private String description;
    private double price;
    private byte[] image;

    private int mainQuantity;
    private int sideQuantity;
    private int drinkQuantity;
    private int secondaryQuantity;
    private int dessertQuantity;

    private List<String> mainProducts = new ArrayList<>();
    private List<String> sideProducts = new ArrayList<>();
    private List<String> drinkProducts = new ArrayList<>();
    private List<String> secondaryProducts = new ArrayList<>();
    private List<String> dessertProducts = new ArrayList<>();

    public MenuSnapshot(Menu menu) {
        this.name = menu.getName();
        this.description = menu.getDescription();
        this.price = menu.getPrice();
        this.image = menu.getImage();

        this.mainQuantity = menu.getMainQuantity();
        this.sideQuantity = menu.getSideQuantity();
        this.drinkQuantity = menu.getDrinkQuantity();
        this.secondaryQuantity = menu.getSecondaryQuantity();
        this.dessertQuantity = menu.getDessertQuantity();

        menu.getMainProducts().forEach(p -> mainProducts.add(p.getName()));
        menu.getSideProducts().forEach(p -> sideProducts.add(p.getName()));
        menu.getDrinkProducts().forEach(p -> drinkProducts.add(p.getName()));
        menu.getSecondaryProducts().forEach(p -> secondaryProducts.add(p.getName()));
        menu.getDessertProducts().forEach(p -> dessertProducts.add(p.getName()));
    }

    public MenuSnapshot(MenuSnapshot other) {
        this.name = other.name;
        this.description = other.description;
        this.price = other.price;
        this.image = other.image;

        this.mainQuantity = other.mainQuantity;
        this.sideQuantity = other.sideQuantity;
        this.drinkQuantity = other.drinkQuantity;
        this.secondaryQuantity = other.secondaryQuantity;
        this.dessertQuantity = other.dessertQuantity;

        this.mainProducts = new ArrayList<>(other.mainProducts);
        this.sideProducts = new ArrayList<>(other.sideProducts);
        this.drinkProducts = new ArrayList<>(other.drinkProducts);
        this.secondaryProducts = new ArrayList<>(other.secondaryProducts);
        this.dessertProducts = new ArrayList<>(other.dessertProducts);
    }

    // GETTERS
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public byte[] getImage() { return image; }

    public int getMainQuantity() { return mainQuantity; }
    public int getSideQuantity() { return sideQuantity; }
    public int getDrinkQuantity() { return drinkQuantity; }
    public int getSecondaryQuantity() { return secondaryQuantity; }
    public int getDessertQuantity() { return dessertQuantity; }

    public List<String> getMainProducts() { return mainProducts; }
    public List<String> getSideProducts() { return sideProducts; }
    public List<String> getDrinkProducts() { return drinkProducts; }
    public List<String> getSecondaryProducts() { return secondaryProducts; }
    public List<String> getDessertProducts() { return dessertProducts; }

    public void setMainProducts(List<Product> products) {
        this.mainProducts = products.stream().map(Product::getName).toList();
    }

    public void setSideProducts(List<Product> products) {
        this.sideProducts = products.stream().map(Product::getName).toList();
    }

    public void setDrinkProducts(List<Product> products) {
        this.drinkProducts = products.stream().map(Product::getName).toList();
    }

    public void setSecondaryProducts(List<Product> products) {
        this.secondaryProducts = products.stream().map(Product::getName).toList();
    }

    public void setDessertProducts(List<Product> products) {
        this.dessertProducts = products.stream().map(Product::getName).toList();
    }
}
