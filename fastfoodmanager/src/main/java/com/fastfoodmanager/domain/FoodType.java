package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
public class FoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public FoodType() {}

    public FoodType(String name) {
        this.name = name;
    }

    // GETTERS / SETTERS
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
