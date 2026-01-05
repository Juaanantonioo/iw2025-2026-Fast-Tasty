package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_card_settings")
public class MenuCardSettings {

    @Id
    private Long id = 1L; // siempre 1

    private String name;

    @Lob
    private byte[] image;

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}
