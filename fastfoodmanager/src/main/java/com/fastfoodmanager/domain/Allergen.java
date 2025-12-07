package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
public class Allergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Allergen() {}

    public Allergen(String name) {
        this.name = name;
    }

    // GETTERS / SETTERS
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
