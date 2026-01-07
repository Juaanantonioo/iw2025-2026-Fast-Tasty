package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class FoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Nueva propiedad para almacenar la imagen en bytes
    @Lob
    private byte[] image;

    public FoodType() {}

    public FoodType(String name) {
        this.name = name;
    }

    // ---------- GETTERS / SETTERS ----------
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodType)) return false;
        FoodType other = (FoodType) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
