package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.MenuService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.util.List;

@PageTitle("Inicio | FastTasty")
@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@CssImport("./themes/my-theme/home.css")
public class HomeView extends VerticalLayout {

    private final MenuService menuService;

    public HomeView(MenuService menuService) {
        this.menuService = menuService;

        addClassName("home-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        // Hero / banner
        Div hero = new Div();
        hero.addClassName("hero-section");

        H1 title = new H1("Bienvenido a Fast&Tasty 🍔");
        Paragraph subtitle = new Paragraph("¡Pide tus comidas favoritas con un solo clic!");
        hero.add(title, subtitle);

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

        // Imagen (si tienes campo image en Product, cámbialo)
        Image img = new Image("https://picsum.photos/seed/" + product.getId() + "/300/200", product.getName());
        img.addClassName("product-image");

        H1 name = new H1(product.getName());
        name.addClassName("product-name");

        Paragraph desc = new Paragraph(product.getDescription());
        desc.addClassName("product-desc");

        Paragraph price = new Paragraph(product.getPrice() + " €");
        price.addClassName("product-price");

        Button order = new Button("Agregar al pedido");
        order.addClassName("product-button");

        card.add(img, name, desc, price, order);
        return card;
    }
}
