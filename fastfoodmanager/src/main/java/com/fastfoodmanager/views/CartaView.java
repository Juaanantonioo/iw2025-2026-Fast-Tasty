package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.MenuService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import com.vaadin.flow.server.auth.AnonymousAllowed;  // 👈
@PageTitle("Carta | FastTasty")
@Route(value = "carta", layout = MainLayout.class)
@CssImport("./themes/my-theme/home.css")
@AnonymousAllowed                                   // 👈 pública
public class CartaView extends VerticalLayout {

    private final MenuService menuService;

    public CartaView(MenuService menuService) {
        this.menuService = menuService;

        addClassName("carta-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        Div hero = new Div();
        hero.addClassName("hero-section");
        H1 title = new H1("Nuestra Carta 🍽️");
        Paragraph subtitle = new Paragraph("Descubre todos nuestros platos disponibles para ti");
        hero.add(title, subtitle);

        Div productGrid = new Div();
        productGrid.addClassName("product-grid");

        List<Product> products = menuService.findActiveProducts();
        for (Product p : products) productGrid.add(createProductCard(p));

        add(hero, productGrid);
    }

    private boolean isAuthenticated() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.isAuthenticated() && !"anonymousUser".equals(a.getPrincipal());
    }

    private Div createProductCard(Product product) {
        Div card = new Div();
        card.addClassName("product-card");

        Image img = new Image("https://picsum.photos/seed/" + product.getId() + "/300/200", product.getName());
        img.addClassName("product-image");

        H1 name = new H1(product.getName());
        name.addClassName("product-name");

        Paragraph desc = new Paragraph(product.getDescription());
        desc.addClassName("product-desc");

        Paragraph price = new Paragraph(product.getPrice() + " €");
        price.addClassName("product-price");

        Button order = new Button("Agregar al pedido", e -> {
            if (!isAuthenticated()) {
                UI.getCurrent().navigate("login");
                return;
            }
            // TODO: lógica de carrito/pedido
        });
        order.addClassName("product-button");

        card.add(img, name, desc, price, order);
        return card;
    }
}
