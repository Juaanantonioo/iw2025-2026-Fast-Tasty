package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.MenuService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Carta | FastTasty")
@Route(value = "carta", layout = MainLayout.class)
@CssImport("./themes/my-theme/home.css") // puedes renombrarlo a carta.css si lo prefieres
public class CartaView extends VerticalLayout {

    private final MenuService menuService;

    public CartaView(MenuService menuService) {
        this.menuService = menuService;

        addClassName("carta-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();

        // Banner o encabezado de la carta
        Div hero = new Div();
        hero.addClassName("hero-section");

        H1 title = new H1("Nuestra Carta 🍽️");
        Paragraph subtitle = new Paragraph("Descubre todos nuestros platos disponibles para ti");
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

        // Imagen temporal (puedes cambiarla por un campo del producto)
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
