package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Carta | FastTasty")
@RolesAllowed({"USER"})
@Route(value = "carta", layout = MainLayout.class)
public class CartaView extends VerticalLayout {

    private static final String CART_KEY = "CART_MAP"; // Map<Long productId, Integer qty>

    private final ProductService productService;

    private final Span carritoCount = new Span();

    public CartaView(ProductService productService) {
        this.productService = productService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("Nuestra Carta");
        title.getStyle().set("margin", "0");

        Button goCart = new Button("Pedido", e -> UI.getCurrent().navigate("client/order"));
        goCart.getStyle()
                .set("border-radius", "10px")
                .set("font-weight", "600");

        updateCartCount();

        HorizontalLayout top = new HorizontalLayout(title, goCart, carritoCount);
        top.setWidthFull();
        top.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        top.expand(title);
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);

        var grid = new com.vaadin.flow.component.html.Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(260px, 1fr))")
                .set("gap", "18px")
                .set("width", "100%");

        List<Product> products = productService.findAll().stream()
                .filter(Product::isActive)
                .toList();

        for (Product p : products) {
            grid.add(buildCard(p));
        }

        add(top, grid);
    }

    private com.vaadin.flow.component.Component buildCard(Product p) {
        var card = new com.vaadin.flow.component.html.Div();
        card.getStyle()
                .set("border", "1px solid #eee")
                .set("border-radius", "16px")
                .set("padding", "14px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.04)")
                .set("background", "white");

        Image img = buildImage(p);
        img.setWidth("100%");
        img.setHeight("180px");
        img.getStyle().set("object-fit", "cover");
        img.getStyle().set("border-radius", "12px");

        var name = new Span(p.getName());
        name.getStyle().set("font-size", "1.1rem").set("font-weight", "700");

        var desc = new Paragraph(p.getDescription() == null ? "" : p.getDescription());
        desc.getStyle().set("margin", "8px 0 10px 0").set("color", "#555");

        var price = new Span(String.format("€ %.2f", p.getPrice() == null ? 0.0 : p.getPrice()));
        price.getStyle().set("font-size", "1.05rem").set("font-weight", "700");

        Button addBtn = new Button("Agregar al pedido", e -> {
            addToCart(p.getId());
            Notification.show("Añadido: " + p.getName(), 1500, Notification.Position.MIDDLE);
        });
        addBtn.getStyle()
                .set("background", "#ff7b00")
                .set("color", "white")
                .set("border-radius", "10px")
                .set("font-weight", "700")
                .set("width", "100%");

        var bottom = new VerticalLayout(price, addBtn);
        bottom.setSpacing(false);
        bottom.setPadding(false);
        bottom.getStyle().set("margin-top", "6px");

        card.add(img, name, desc, bottom);
        return card;
    }

    private Image buildImage(Product p) {
        byte[] bytes = p.getImage();
        if (bytes == null || bytes.length == 0) {
            // Si no hay imagen guardada, mostramos un placeholder simple
            Image img = new Image();
            img.setAlt("Sin imagen");
            img.getElement().setProperty("src", "");
            return img;
        }

        StreamResource res = new StreamResource(
                "product-" + p.getId() + ".img",
                () -> new ByteArrayInputStream(bytes)
        );

        return new Image(res, "Imagen producto");
    }

    private void addToCart(Long productId) {
        Map<Long, Integer> cart = getCart();
        cart.put(productId, cart.getOrDefault(productId, 0) + 1);
        VaadinSession.getCurrent().setAttribute(CART_KEY, cart);
        updateCartCount();
    }

    private void updateCartCount() {
        Map<Long, Integer> cart = getCart();
        int totalUnits = cart.values().stream().mapToInt(Integer::intValue).sum();
        carritoCount.setText("En carrito: " + totalUnits);
        carritoCount.getStyle().set("font-weight", "700");
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart() {
        Object o = VaadinSession.getCurrent().getAttribute(CART_KEY);
        if (o instanceof Map<?, ?> map) {
            try {
                return (Map<Long, Integer>) map;
            } catch (Exception ignored) {}
        }
        Map<Long, Integer> fresh = new HashMap<>();
        VaadinSession.getCurrent().setAttribute(CART_KEY, fresh);
        return fresh;
    }
}
