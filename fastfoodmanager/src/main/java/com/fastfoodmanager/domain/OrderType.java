package com.fastfoodmanager.domain;

public enum OrderType {
    PICKUP("Recoger en local"),
    DELIVERY("Domicilio");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}