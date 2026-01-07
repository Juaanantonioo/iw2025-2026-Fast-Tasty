package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
public class OfferCardSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name = "Ofertas";

    @Lob
    private byte[] image;

    public OfferCardSettings() {}

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
}
