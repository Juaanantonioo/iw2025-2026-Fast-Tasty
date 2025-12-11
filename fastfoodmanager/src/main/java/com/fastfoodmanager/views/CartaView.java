package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.service.MenuService;
import com.fastfoodmanager.service.CartService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

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

    private Div productGrid; // <- ahora es atributo de clase

    public CartaView(MenuService menuService, CartService cartService) {
        this.menuService = menuService;
        this.cartService = cartService;

        addClassName("carta-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // HERO
        Div hero = new Div();
        hero.addClassName("hero-section");
        hero.getStyle().set("background", "linear-gradient(90deg, #ffb86b, #ff7b00)");
        hero.getStyle().set("border-radius", "12px");
        hero.getStyle().set("padding", "26px 32px");
        hero.getStyle().set("width", "86%");
        hero.getStyle().set("margin", "30px auto");
        hero.getStyle().set("color", "white");
        hero.getStyle().set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");

        // GRID fila título + botón
        Div gridRow = new Div();
        gridRow.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr auto 1fr")
                .set("align-items", "center")
                .set("gap", "16px");

        H1 title = new H1("Nuestra Carta 🍽️");
        title.addClassName("hero-title");
        title.getStyle().set("justify-self", "center");

        Button viewCartButton = new Button("🧾 Pedido", e -> UI.getCurrent().navigate("carrito"));
        viewCartButton.addClassName("hero-pill");

        Div rightBox = new Div(viewCartButton);
        rightBox.getStyle().set("justify-self", "end");

        Button filterButton = new Button("🔍 Filtrar");
        filterButton.addClassName("hero-pill");
        filterButton.addClickListener(e -> openFilterMenu());

        Div leftBox = new Div(filterButton);
        leftBox.getStyle().set("justify-self", "start");

        gridRow.add(leftBox, title, rightBox);

        Paragraph subtitle = new Paragraph("Descubre todos nuestros platos disponibles para ti");
        subtitle.addClassName("hero-subtitle");

        hero.add(gridRow, subtitle);

        // GRID Productos
        productGrid = new Div();
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

    private boolean isAuthenticated() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.isAuthenticated() && !"anonymousUser".equals(String.valueOf(a.getPrincipal()));
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

        H1 name = new H1(product.getName());
        name.addClassName("product-name");
        name.getStyle().set("color", "#ff7b00");
        name.getStyle().set("font-size", "1.35rem");
        name.getStyle().set("margin", "8px 0 0");

        // Validación de la URL de la imagen
        String imageUrl = product.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = "path/to/default/image.jpg"; // Imagen predeterminada
        }
        Image img = new Image(imageUrl, product.getName());
        img.setWidth("180px");
        img.getStyle().set("border-radius", "12px");
        img.getStyle().set("margin", "0 auto");

        Paragraph price = new Paragraph(currency.format(java.math.BigDecimal.valueOf(product.getPrice())));
        price.addClassName("product-price");
        price.getStyle().set("font-weight", "bold");
        price.getStyle().set("margin", "8px 0 10px");

        Button addToCart = new Button("Agregar al Pedido", e -> {
            if (!isAuthenticated()) {
                UI.getCurrent().navigate("login");
                return;
            }

            cartService.addProduct(product);

            Notification notification = Notification.show("Añadido al pedido", 2000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        addToCart.getStyle().set("background-color", "#ff7b00");
        addToCart.getStyle().set("color", "white");
        addToCart.getStyle().set("font-weight", "700");
        addToCart.getStyle().set("border-radius", "8px");
        addToCart.getStyle().set("width", "100%");
        addToCart.getStyle().set("margin-top", "8px");

        Button detailsBtn = new Button("Ver detalles", e -> {
            System.out.println("Detalles de: " + product.getName());
            openDetailsDialog(product);
        });
        detailsBtn.getStyle().set("background-color", "#ff7b00");
        detailsBtn.getStyle().set("color", "white");
        detailsBtn.getStyle().set("font-weight", "600");
        detailsBtn.getStyle().set("border-radius", "8px");
        detailsBtn.getStyle().set("width", "100%");
        detailsBtn.getStyle().set("margin-top", "8px");

        card.add(img, name, price, addToCart, detailsBtn);
        return card;
    }

    private void openDetailsDialog(Product product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(product.getName());

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        // Validación de la URL de la imagen
        String imageUrl = product.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = "path/to/default/image.jpg"; // Imagen predeterminada
        }
        Image bigImg = new Image(imageUrl, product.getName());
        bigImg.setWidth("420px");
        bigImg.getStyle()
                .set("border-radius", "16px")
                .set("margin", "0 auto 20px auto")
                .set("display", "block")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");

        Paragraph desc = new Paragraph(
                product.getDescription() != null && !product.getDescription().isBlank()
                        ? product.getDescription()
                        : "Sin descripción disponible."
        );

        Paragraph price = new Paragraph("Precio: " +
                currency.format(java.math.BigDecimal.valueOf(product.getPrice())));
        price.getStyle().set("font-weight", "700");

        // Listar Alérgenos
        UnorderedList allergensList = new UnorderedList();

        if (product.getAllergens() != null && !product.getAllergens().isEmpty()) {
            product.getAllergens().forEach(a ->
                    allergensList.add(new ListItem(a.getName()))
            );
        } else {
            allergensList.add(new ListItem("— Sin información de alérgenos —"));
        }

        content.add(bigImg, desc, price, new H3("Alérgenos"), allergensList);
        dialog.add(content);

        // ✔ Mostrar notificación tras cerrarse el diálogo
        dialog.addOpenedChangeListener(ev -> {
            boolean opened = ev.isOpened();
            boolean pending = dialog.getElement().getProperty("showAddedNotification", false);

            if (!opened && pending) {
                dialog.getElement().setProperty("showAddedNotification", false);

                Notification notif = Notification.show(
                        "Añadido al pedido",
                        2000,
                        Notification.Position.MIDDLE
                );
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        Button addButton = new Button("Agregar al Pedido", e -> {
            if (!isAuthenticated()) {
                UI.getCurrent().navigate("login");
                return;
            }

            cartService.addProduct(product);

            // ✔ indicar que debe mostrarse la notificación
            dialog.getElement().setProperty("showAddedNotification", true);

            dialog.close(); // el mensaje aparecerá DESPUÉS
        });
        addButton.getStyle().set("background-color", "#ff7b00")
                .set("color", "white")
                .set("font-weight", "700")
                .set("border-radius", "8px");

        Button close = new Button("Cerrar", e -> dialog.close());
        dialog.getFooter().add(close, addButton);

        dialog.setWidth("720px");
        dialog.open();
    }

    private void openFilterMenu() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtrar productos");

        VerticalLayout content = new VerticalLayout();

        // SELECT Tipo
        Select<FoodType> typeSelect = new Select<>();
        typeSelect.setLabel("Tipo");
        typeSelect.setItemLabelGenerator(FoodType::getName);
        List<FoodType> types = menuService.findAllFoodTypes();
        typeSelect.setItems(types != null ? types : List.of());

        // SELECT Alérgenos
        Select<Allergen> allergenSelect = new Select<>();
        allergenSelect.setLabel("Alérgenos");
        allergenSelect.setItemLabelGenerator(a -> "Sin " + a.getName());
        List<Allergen> allergens = menuService.findAllAllergens();
        allergenSelect.setItems(allergens != null ? allergens : List.of());

        Button apply = new Button("Aplicar filtros", e -> {
            // IDs de alérgenos a excluir
            List<Long> allergenIds = allergenSelect.getValue() != null
                    ? List.of(allergenSelect.getValue().getId())
                    : List.of();

            // Traer productos activos que NO tengan los alérgenos seleccionados
            List<Product> products = menuService.findActiveWithoutAllergens(allergenIds);

            // Filtrar también por tipo de comida si se seleccionó
            FoodType selectedType = typeSelect.getValue();
            if (selectedType != null) {
                products = products.stream()
                        .filter(p -> p.getType() != null && p.getType().getId().equals(selectedType.getId()))
                        .toList();
            }

            // Limpiar grid y mostrar productos filtrados
            productGrid.removeAll();
            products.forEach(p -> productGrid.add(createProductCard(p)));

            dialog.close();
        });

        Button close = new Button("Cerrar", e -> dialog.close());

        content.add(typeSelect, allergenSelect);
        dialog.add(content);
        dialog.getFooter().add(close, apply);
        dialog.open();
    }

    private void applyFilters(FoodType type, Allergen allergen) {
        productGrid.removeAll();

        menuService.findActiveProducts().stream()
                // Filtrar por tipo
                .filter(p -> type == null || (p.getType() != null && p.getType().getId().equals(type.getId())))
                // Filtrar por alérgeno: comparar por nombre
                .filter(p -> allergen == null ||
                        (p.getAllergens() != null && p.getAllergens().stream()
                                .anyMatch(a -> a.getName().equalsIgnoreCase(allergen.getName()))))
                .forEach(p -> productGrid.add(createProductCard(p)));
    }

}
