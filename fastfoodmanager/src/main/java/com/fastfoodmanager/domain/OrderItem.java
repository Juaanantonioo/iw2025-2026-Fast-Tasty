package com.fastfoodmanager.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Snapshots en memoria (no persistidos)
    @Transient
    private ProductSnapshot product;

    @Transient
    private MenuSnapshot menu;

    // Datos persistidos
    @Column(name = "product_name")
    private String productName;

    @Column(name = "menu_name")
    private String menuName;

    // Ingredientes serializados
    @Lob
    @Column(name = "product_ingredients_json")
    private String productIngredientsJson;

    // Pedido al que pertenece este item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double unitPrice;

    public OrderItem() {}

    private String offerName;

    // ============================
    // PRODUCTO
    // ============================
    public OrderItem(ProductSnapshot snapshot, int quantity) {
        this.product = snapshot;
        this.productName = snapshot.getName();
        this.menu = null;
        this.menuName = null;
        this.quantity = quantity;
        this.unitPrice = snapshot.getPrice();

        try {
            this.productIngredientsJson = MAPPER.writeValueAsString(snapshot.getIngredients());
        } catch (Exception ignored) {}
    }

    public OrderItem(Product product, int quantity) {
        this(new ProductSnapshot(product), quantity);
    }

    // ============================
    // MENÚ
    // ============================
    public OrderItem(Menu menu, int quantity) {
        this.menu = new MenuSnapshot(menu);
        this.menuName = menu.getName();
        this.product = null;
        this.productName = null;
        this.quantity = quantity;
        this.unitPrice = menu.getPrice();
        this.productIngredientsJson = null;
    }

    // ============================
    // OFERTAS
    // ============================
    public OrderItem(String offerName, int quantity) {
        this.offerName = offerName;
        this.quantity = quantity;
    }

    public OrderItem(MenuSnapshot snapshot, int quantity) {
        this.menu = snapshot;
        this.menuName = snapshot.getName();
        this.product = null;
        this.productName = null;
        this.quantity = quantity;
        this.unitPrice = snapshot.getPrice();
        this.productIngredientsJson = null;
    }

    // ============================
    // Reconstrucción del snapshot al cargar desde BD
    // ============================
    @PostLoad
    private void rebuildSnapshot() {
        try {
            if (productName != null && productIngredientsJson != null) {

                List<Product.Ingredient> ingredients =
                        MAPPER.readValue(productIngredientsJson,
                                new TypeReference<List<Product.Ingredient>>() {});

                this.product = new ProductSnapshot(productName, unitPrice, ingredients);
            }
        } catch (Exception ignored) {}
    }


    // ============================
    // GETTERS / SETTERS
    // ============================
    public Long getId() { return id; }

    public ProductSnapshot getProduct() { return product; }
    public MenuSnapshot getMenu() { return menu; }

    public String getProductName() { return productName; }
    public String getMenuName() { return menuName; }

    public boolean isMenu() { return menuName != null; }
    public boolean isProduct() { return productName != null; }

    public void setProduct(ProductSnapshot snapshot) {
        this.product = snapshot;
        this.productName = snapshot.getName();
        this.menu = null;
        this.menuName = null;
        this.unitPrice = snapshot.getPrice();

        try {
            this.productIngredientsJson = MAPPER.writeValueAsString(snapshot.getIngredients());
        } catch (Exception ignored) {}
    }

    public void setMenu(MenuSnapshot snapshot) {
        this.menu = snapshot;
        this.menuName = snapshot.getName();
        this.product = null;
        this.productName = null;
        this.unitPrice = snapshot.getPrice();
        this.productIngredientsJson = null;
    }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getOfferName() { return offerName; }
    public void setOfferName(String offerName) { this.offerName = offerName; }

    public double getSubtotal() {
        return unitPrice * quantity;
    }

    // ============================
    // REPRESENTACIÓN
    // ============================
    @Override
    public String toString() {
        if (isMenu()) {
            return menuName + " (Menú) x" + quantity + " (" + unitPrice + "€)";
        }
        return productName + " x" + quantity + " (" + unitPrice + "€)";
    }

    // ============================
    // EQUALS / HASHCODE
    // ============================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;

        return quantity == that.quantity &&
                Double.compare(that.unitPrice, unitPrice) == 0 &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(menuName, that.menuName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, menuName, quantity, unitPrice);
    }
}
