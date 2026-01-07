package com.fastfoodmanager.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre visible en el panel de administración
    private String name;

    // PRODUCT, CATEGORY, GLOBAL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferTarget targetType;

    // DISCOUNT, ZxY
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferMode mode;

    // ============================
    // DESCUENTO (%)
    // ============================
    // Solo se usa si mode = DISCOUNT
    private Integer discountPercentage;

    // ============================
    // ZxY (ej: 2x1, 3x2, 5x3)
    // ============================
    // Solo se usa si mode = ZxY
    private Integer zValue; // Z productos
    private Integer yValue; // Se pagan Y

    @Lob
    private byte[] image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToOne
    private OfferCardSettings cardSettings;

    // ============================
    // TARGETS
    // ============================

    // Si targetType = PRODUCT → se usan estos productos
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "offer_products",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    // Si targetType = CATEGORY → se usan estas categorías
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "offer_categories",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<FoodType> categories = new ArrayList<>();

    // ============================
    // FECHAS
    // ============================
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Activar / desactivar oferta
    private boolean active = true;

    public void clearTargets() {
        this.products.clear();
        this.categories.clear();
    }

    // ============================
    // GETTERS & SETTERS
    // ============================

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public OfferTarget getTargetType() { return targetType; }
    public void setTargetType(OfferTarget targetType) { this.targetType = targetType; }

    public OfferMode getMode() { return mode; }
    public void setMode(OfferMode mode) { this.mode = mode; }

    public Integer getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Integer discountPercentage) { this.discountPercentage = discountPercentage; }

    public Integer getZValue() { return zValue; }
    public void setZValue(Integer zValue) { this.zValue = zValue; }

    public Integer getYValue() { return yValue; }
    public void setYValue(Integer yValue) { this.yValue = yValue; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public List<FoodType> getCategories() { return categories; }
    public void setCategories(List<FoodType> categories) { this.categories = categories; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
