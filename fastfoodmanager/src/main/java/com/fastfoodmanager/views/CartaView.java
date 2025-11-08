package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.MenuService;
import com.fastfoodmanager.service.CartService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@PageTitle("Carta | FastTasty")
@Route(value = "carta", layout = MainLayout.class)
@CssImport("./themes/my-theme/home.css")
@AnonymousAllowed
public class CartaView extends VerticalLayout {

    private final MenuService menuService;
    private final CartService cartService;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    public CartaView(MenuService menuService, CartService cartService) {
        this.menuService = menuService;
        this.cartService = cartService;

        addClassName("carta-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // ===== HERO =====
        Div hero = new Div();
        hero.addClassName("hero-section");
        hero.getStyle().set("background", "linear-gradient(90deg, #ffb86b, #ff7b00)");
        hero.getStyle().set("border-radius", "12px");
        hero.getStyle().set("padding", "30px 40px");
        hero.getStyle().set("width", "86%");
        hero.getStyle().set("margin", "30px auto");
        hero.getStyle().set("color", "white");
        hero.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");

        // Título
        H1 title = new H1("Nuestra Carta 🍽️");
        title.getStyle().set("margin", "0");
        title.getStyle().set("font-weight", "800");
        title.getStyle().set("font-size", "2.1rem");
        title.getStyle().set("color", "#1f2937");

        // Botón Pedido (blanco)
        Button viewCartButton = new Button("🧾 Pedido", e -> UI.getCurrent().navigate("carrito"));
        viewCartButton.getStyle().set("background-color", "#ffffff");
        viewCartButton.getStyle().set("color", "#ff7b00");
        viewCartButton.getStyle().set("font-weight", "700");
        viewCartButton.getStyle().set("border-radius", "10px");
        viewCartButton.getStyle().set("padding", "10px 24px");
        viewCartButton.getStyle().set("font-size", "16px");
        viewCartButton.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,.15)");

        // Botón Gestionar (solo ADMIN)
        Button manageBtn = new Button("Gestionar productos", e -> UI.getCurrent().navigate("/products"));
        manageBtn.getStyle().set("background-color", "#111827");
        manageBtn.getStyle().set("color", "white");
        manageBtn.getStyle().set("font-weight", "700");
        manageBtn.getStyle().set("border-radius", "10px");
        manageBtn.getStyle().set("padding", "10px 16px");
        manageBtn.setVisible(hasRole("ADMIN"));

        HorizontalLayout rightControls = new HorizontalLayout(manageBtn, viewCartButton);
        rightControls.setSpacing(true);
        rightControls.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout titleRow = new HorizontalLayout(title, rightControls);
        titleRow.setWidthFull();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Paragraph subtitle = new Paragraph("Descubre todos nuestros platos disponibles para ti");
        subtitle.getStyle().set("margin", "8px 0 0");
        subtitle.getStyle().set("color", "#222");
        subtitle.getStyle().set("text-align", "center");

        hero.add(titleRow, subtitle);

        // ===== GRID =====
        Div productGrid = new Div();
        productGrid.addClassName("product-grid");
        productGrid.getStyle().set("display", "flex");
        productGrid.getStyle().set("flex-wrap", "wrap");
        productGrid.getStyle().set("justify-content", "center");
        productGrid.getStyle().set("gap", "22px");
        productGrid.getStyle().set("max-width", "1200px");

        List<Product> products = menuService.findActiveProducts();
        for (Product p : products) productGrid.add(createProductCard(p));

        add(hero, productGrid);
    }

    private boolean hasRole(String role) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return false;
        String needed = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        for (GrantedAuthority ga : a.getAuthorities()) {
            if (needed.equals(ga.getAuthority())) return true;
        }
        return false;
    }

    private Div createProductCard(Product product) {
        Div card = new Div();
        card.addClassName("product-card");
        card.getStyle().set("padding", "14px");
        card.getStyle().set("border-radius", "12px");
        card.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        card.getStyle().set("text-align", "center");
        card.getStyle().set("max-width", "260px");
        card.getStyle().set("background-color", "white");

        Image img = new Image("https://picsum.photos/seed/" + product.getId() + "/300/200", product.getName());
        img.addClassName("product-image");
        img.getStyle().set("border-radius", "8px");

        H1 name = new H1(product.getName());
        name.addClassName("product-name");
        name.getStyle().set("color", "#ff7b00");
        name.getStyle().set("font-size", "1.35rem");
        name.getStyle().set("margin", "8px 0 0");

        Paragraph price = new Paragraph(currency.format(java.math.BigDecimal.valueOf(product.getPrice())));
        price.addClassName("product-price");
        price.getStyle().set("font-weight", "bold");
        price.getStyle().set("margin", "8px 0 10px");

        Button addToCart = new Button("Agregar al Pedido", e -> cartService.addProduct(product));
        addToCart.getStyle().set("background-color", "#ff7b00");
        addToCart.getStyle().set("color", "white");
        addToCart.getStyle().set("font-weight", "700");
        addToCart.getStyle().set("border-radius", "8px");
        addToCart.getStyle().set("width", "100%");
        addToCart.getStyle().set("margin-top", "8px");

        Button detailsBtn = new Button("Ver detalles", e -> openDetailsDialog(product));
        detailsBtn.getStyle().set("background-color", "#ff7b00");
        detailsBtn.getStyle().set("color", "white");
        detailsBtn.getStyle().set("font-weight", "600");
        detailsBtn.getStyle().set("border-radius", "8px");
        detailsBtn.getStyle().set("width", "100%");
        detailsBtn.getStyle().set("margin-top", "8px");

        card.add(img, name, price, addToCart, detailsBtn);
        return card;
    }

    /** Dialog con descripción, alérgenos (placeholder) y precio. */
    private void openDetailsDialog(Product product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(product.getName());

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        Image bigImg = new Image("https://picsum.photos/seed/" + product.getId() + "/600/350", product.getName());
        bigImg.getStyle().set("border-radius", "10px");
        bigImg.getStyle().set("margin-bottom", "10px");

        Paragraph desc = new Paragraph(
                product.getDescription() != null && !product.getDescription().isBlank()
                        ? product.getDescription()
                        : "Sin descripción disponible."
        );

        Paragraph price = new Paragraph("Precio: " +
                currency.format(java.math.BigDecimal.valueOf(product.getPrice())));
        price.getStyle().set("font-weight", "700");

        UnorderedList allergensList = new UnorderedList();
        allergensList.add(new ListItem("Consulta al personal por posibles alérgenos."));

        content.add(bigImg, desc, price, new H3("Alérgenos"), allergensList);
        dialog.add(content);

        Button addButton = new Button("Agregar al Pedido", e -> {
            cartService.addProduct(product);
            dialog.close();
        });
        addButton.getStyle().set("background-color", "#ff7b00");
        addButton.getStyle().set("color", "white");
        addButton.getStyle().set("font-weight", "700");
        addButton.getStyle().set("border-radius", "8px");

        // ⛔️ Botón correcto: Cerrar (NO cerrar sesión)
        Button close = new Button("Cerrar", e -> dialog.close());
        dialog.getFooter().add(close, addButton);

        dialog.setWidth("720px");
        dialog.open();
    }
}
