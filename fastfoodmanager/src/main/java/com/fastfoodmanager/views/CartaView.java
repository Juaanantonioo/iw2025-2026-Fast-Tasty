package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.CartService;
import com.fastfoodmanager.service.MenuService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


import java.util.ArrayList;
import java.util.List;

@PageTitle("Carta | FastTasty")
@Route(value = "carta", layout = MainLayout.class)
//@CssImport("./themes/my-theme/carta.css")

public class CartaView extends VerticalLayout {

    private final MenuService menuService;
    private final CartService cartService;
    private Button cartButton;
    private Span itemCount;

    public CartaView(MenuService menuService, CartService cartService) {
        this.menuService = menuService;
        this.cartService = cartService;

        addClassName("carta-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        createHeader();
        createContent();

        // Actualizar el contador inicial
        updateCartBadge();
    }

    private void createHeader() {
        // Layout para el header con título y carrito
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addClassName("carta-header");
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        // Título
        Div titleSection = new Div();
        titleSection.addClassName("title-section");

        H1 title = new H1("Nuestra Carta 🍽️");
        Paragraph subtitle = new Paragraph("Descubre todos nuestros platos disponibles para ti");
        titleSection.add(title, subtitle);

        // Botón del carrito
        createCartButton();

        headerLayout.add(titleSection, cartButton);
        add(headerLayout);
    }

    private void createCartButton() {
        cartButton = new Button();
        cartButton.addClassName("cart-button");

        Icon cartIcon = VaadinIcon.CART.create();
        itemCount = new Span("0");
        itemCount.addClassName("cart-item-count");

        cartButton.setText("Ver Carrito");
        cartButton.setIcon(cartIcon);

        // Actualizar el contador inicial
        updateCartBadge();

        // Navegar a la vista del carrito al hacer clic
        cartButton.addClickListener(e -> {
            cartButton.getUI().ifPresent(ui ->
                    ui.navigate("carrito"));
        });
    }

    private void createContent() {
        // Banner hero
        Div hero = new Div();
        hero.addClassName("hero-section");
        hero.add(new H1("Deliciosos Platos"),
                new Paragraph("Selecciona tus favoritos"));

        // Sección productos
        Div productGrid = new Div();
        productGrid.addClassName("product-grid");

        List<Product> products = menuService.findActiveProducts();
        for (Product p : products) {
            Div card = createProductCard(p);
            productGrid.add(card);
        }

        add(hero, productGrid);
    }

    private Div createProductCard(Product product) {
        Div card = new Div();
        card.addClassName("product-card");

        // Imagen del producto
        Image img = new Image("https://picsum.photos/seed/" + product.getId() + "/300/200", product.getName());
        img.addClassName("product-image");

        H1 name = new H1(product.getName());
        name.addClassName("product-name");

        Paragraph desc = new Paragraph(product.getDescription());
        desc.addClassName("product-desc");

        Paragraph price = new Paragraph(String.format("%.2f €", product.getPrice()));
        price.addClassName("product-price");

        Button order = new Button("Agregar al pedido");
        order.addClassName("product-button");

        // Añadir funcionalidad al botón
        order.addClickListener(e -> {
            addToCart(product);
            Notification.show(product.getName() + " añadido al carrito", 2000, Notification.Position.BOTTOM_END);
        });

        card.add(img, name, desc, price, order);
        return card;
    }

    private void addToCart(Product product) {
        cartService.addProduct(product);
        updateCartBadge();
    }

    private void updateCartBadge() {
        int itemCountValue = cartService.getItemCount();
        itemCount.setText(String.valueOf(itemCountValue));

        if (itemCountValue > 0) {
            cartButton.addClassName("cart-has-items");
        } else {
            cartButton.removeClassName("cart-has-items");
        }
    }

}