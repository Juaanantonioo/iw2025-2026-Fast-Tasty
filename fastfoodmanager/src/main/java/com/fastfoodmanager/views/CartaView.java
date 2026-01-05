package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Menu;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.domain.MenuCardSettings;
import com.fastfoodmanager.domain.MenuSnapshot;
import com.fastfoodmanager.domain.ProductSnapshot;
import com.fastfoodmanager.service.MenusService;
import com.fastfoodmanager.service.MenuCardSettingsService;
import com.fastfoodmanager.service.MenuService;
import com.fastfoodmanager.service.MenusService;
import com.fastfoodmanager.service.CartService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.NumberFormat;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.function.BiConsumer;

@PageTitle("Carta | FastTasty")
@Route(value = "carta", layout = MainLayout.class)
@CssImport("./themes/my-theme/home.css")
@AnonymousAllowed
public class CartaView extends VerticalLayout {

    private final MenuService menuService;
    private final MenusService menusService;
    private final CartService cartService;
    private final MenuCardSettingsService cardSettingsService;
    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

    private Div productGrid;
    private boolean showingFoodTypes = true;

    public CartaView(MenuService menuService,
                     CartService cartService,
                     MenusService menusService,
                     MenuCardSettingsService cardSettingsService) {
        this.menuService = menuService;
        this.cartService = cartService;
        this.menusService = menusService;
        this.cardSettingsService = cardSettingsService;

        addClassName("carta-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        // HERO
        Div hero = new Div();
        hero.addClassName("hero-section");
        hero.getStyle()
                .set("background", "linear-gradient(90deg, #ffb86b, #ff7b00)")
                .set("border-radius", "12px")
                .set("padding", "26px 32px")
                .set("width", "86%")
                .set("margin", "30px auto")
                .set("color", "white")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");

        // GRID título + botones
        Div gridRow = new Div();
        gridRow.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr auto 1fr")
                .set("align-items", "center")
                .set("gap", "16px");

        H1 title = new H1("Nuestra Carta 🍽️");
        title.addClassName("hero-title");
        title.getStyle().set("justify-self", "center");

        Button viewCartButton =
                new Button("🧾 Pedido", e -> UI.getCurrent().navigate("carrito"));
        viewCartButton.addClassName("hero-pill");

        Div rightBox = new Div(viewCartButton);
        rightBox.getStyle().set("justify-self", "end");

        Button filterButton = new Button("🔍 Filtrar");
        filterButton.addClassName("hero-pill");
        filterButton.addClickListener(e -> openFilterMenu());

        Div leftBox = new Div(filterButton);
        leftBox.getStyle().set("justify-self", "start");

        gridRow.add(leftBox, title, rightBox);

        Paragraph subtitle =
                new Paragraph("Descubre todos nuestros platos disponibles para ti");
        subtitle.addClassName("hero-subtitle");

        hero.add(gridRow, subtitle);

        // GRID
        productGrid = new Div();
        productGrid.addClassName("product-grid");
        productGrid.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("justify-content", "center")
                .set("gap", "22px")
                .set("max-width", "1200px");

        add(hero, productGrid);

        showFoodTypes();
    }

    /* =========================
       FOOD TYPES + MENÚS
       ========================= */

    private void showFoodTypes() {
        showingFoodTypes = true;
        productGrid.removeAll();

        // ⭐ CARD MENÚS
        MenuCardSettings settings = cardSettingsService.get();

        Image menuImg = new Image();
        if (settings.getImage() != null) {
            menuImg.setSrc("data:image/png;base64," +
                    Base64.getEncoder().encodeToString(settings.getImage()));
        } else {
            menuImg.setSrc("path/to/default/menu.png");
        }

        H1 menuTitle = new H1(settings.getName());
        Div menuCard = new Div();
        menuCard.addClassName("product-card");
        menuCard.getStyle().set("cursor", "pointer")
                .set("padding", "14px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("text-align", "center")
                .set("max-width", "260px")
                .set("background-color", "white");

        menuImg.setWidth("180px");
        menuImg.getStyle().set("border-radius", "12px");

        menuTitle.getStyle()
                .set("color", "#ff7b00")
                .set("font-size", "1.6rem")
                .set("margin", "12px 0");

        menuCard.add(menuImg, menuTitle);
        menuCard.addClickListener(e -> showMenus());

        // ⭐ AÑADIRLO AL GRID
        productGrid.add(menuCard);

        // ⭐ FOOD TYPES
        menuService.findAllFoodTypes().forEach(type -> {
            Div card = new Div();
            card.addClassName("product-card");
            card.getStyle().set("cursor", "pointer")
                    .set("padding", "14px")
                    .set("border-radius", "12px")
                    .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                    .set("text-align", "center")
                    .set("max-width", "260px")
                    .set("background-color", "white");

            Image img = new Image();
            img.setWidth("180px");
            img.getStyle().set("border-radius", "12px");

            if (type.getImage() != null && type.getImage().length > 0) {
                String base64 = Base64.getEncoder().encodeToString(type.getImage());
                img.setSrc("data:image/png;base64," + base64);
            } else {
                img.setSrc("path/to/default/category.png");
            }

            H1 name = new H1(type.getName());
            name.getStyle()
                    .set("color", "#ff7b00")
                    .set("font-size", "1.6rem")
                    .set("margin", "12px 0");

            card.add(img, name);
            card.addClickListener(e -> showProductsByType(type));

            productGrid.add(card);
        });
    }

    /* =========================
       MENÚS
       ========================= */

    private void showMenus() {
        showingFoodTypes = false;
        productGrid.removeAll();

        Button back = new Button("← Volver");
        styleButton(back, 600);
        back.addClickListener(e -> showFoodTypes());

        Div backWrapper = new Div(back);
        backWrapper.getStyle().set("width", "100%");
        productGrid.add(backWrapper);

        menusService.findActiveMenus().forEach(menu -> productGrid.add(createMenuCard(menu)));
    }

    private Div createMenuCard(Menu menu) {
        Div card = new Div();
        card.addClassName("product-card");
        card.getStyle()
                .set("padding", "14px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("text-align", "center")
                .set("max-width", "260px")
                .set("background-color", "white");

        H1 name = new H1(menu.getName());
        name.getStyle()
                .set("color", "#ff7b00")
                .set("font-size", "1.35rem")
                .set("margin", "8px 0 0");

        Image img = new Image(getMenuImage(menu), menu.getName());
        img.setWidth("180px");
        img.getStyle().set("border-radius", "12px");

        Paragraph price = new Paragraph(currency.format(menu.getPrice()));
        price.getStyle().set("font-weight", "bold");

        Paragraph desc = new Paragraph(menu.getDescription() != null ? menu.getDescription() : "");
        desc.getStyle().set("font-size", "0.9rem").set("min-height", "40px");

        Button add = new Button("Agregar al Pedido", e -> {
            if (!isAuthenticated()) {
                UI.getCurrent().navigate("login");
                return;
            }
            cartService.addMenu(menu);
            Notification n = Notification.show(
                    "Menú añadido al pedido", 2000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        styleButton(add);

        Button details = new Button("Ver detalles", e -> openMenuDetails(menu));
        styleButton(details, 600);

        card.add(img, name, price, desc, add, details);
        return card;
    }

    private String getMenuImage(Menu menu) {
        if (menu.getImage() != null && menu.getImage().length > 0) {
            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(menu.getImage());
        }
        return "path/to/default/menu.png";
    }

    private void openMenuProductCardboard(Product product,
                                          Map<String, Product> selectedProducts,
                                          String category) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(product.getName());

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);content.setPadding(false);

        Image bigImg = new Image(getImageDataUrl(product), product.getName());
        bigImg.setWidth("420px");

        Paragraph desc = new Paragraph(
                product.getDescription() != null
                        ? product.getDescription()
                        : "Sin descripción disponible");

        Paragraph price =
                new Paragraph("Precio: " + currency.format(product.getPrice()));

        /* =========================
           INGREDIENTES NORMALES
           ========================= */
        VerticalLayout ingredientsLayout = new VerticalLayout();
        ingredientsLayout.setSpacing(false);
        ingredientsLayout.setPadding(false);

        if (product.getIngredients() != null && !product.getIngredients().isEmpty()) {
            product.getIngredients().forEach(ingredient -> {

                Div row = new Div();
                row.getStyle()
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "space-between")
                        .set("gap", "12px")
                        .set("width", "100%");

                Span name = new Span(ingredient.getName());
                name.getStyle().set("flex", "1");

                Span quantity = new Span(String.valueOf((int) ingredient.getQuantity()));
                quantity.getStyle()
                        .set("min-width", "24px")
                        .set("text-align", "center")
                        .set("font-weight", "bold");

                row.add(name, quantity);
                ingredientsLayout.add(row);
            });
        } else {
            ingredientsLayout.add(new Span("— Sin ingredientes —"));
        }

        /* =========================
           INGREDIENTES PERSONALIZABLES
           ========================= */
        VerticalLayout customizableLayout = new VerticalLayout();
        customizableLayout.setSpacing(false);
        customizableLayout.setPadding(false);

        List<Product.Ingredient> customizableIngredients =
                product.getIngredients().stream()
                        .filter(Product.Ingredient::isCustomizable)
                        .toList();

        if (customizableIngredients.isEmpty()) {
            customizableLayout.add(new Span("— No hay ingredientes personalizables —"));
        } else {
            customizableIngredients.forEach(ingredient -> {

                Div row = new Div();
                row.getStyle()
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "space-between")
                        .set("gap", "12px")
                        .set("width", "100%");

                Span name = new Span(ingredient.getName());
                name.getStyle().set("flex", "1");

                Span quantity = new Span(String.valueOf((int) ingredient.getQuantity()));
                quantity.getStyle()
                        .set("min-width", "24px")
                        .set("text-align", "center")
                        .set("font-weight", "bold");

                Button minus = new Button("−");
                minus.addClickListener(e -> {
                    int q = Integer.parseInt(quantity.getText());
                    if (q > 0) {
                        q--;
                        quantity.setText(String.valueOf(q));
                        ingredient.setQuantity(q);
                    }
                });

                Button plus = new Button("+");
                plus.addClickListener(e -> {
                    int q = Integer.parseInt(quantity.getText());
                    if (q < 3) {
                        q++;
                        quantity.setText(String.valueOf(q));
                        ingredient.setQuantity(q);
                    }
                });

                minus.getStyle().set("min-width", "32px").set("background-color", "#eee");
                plus.getStyle().set("min-width", "32px").set("background-color", "#eee");

                row.add(name, minus, quantity, plus);
                customizableLayout.add(row);
            });
        }

        /* =========================
           ALÉRGENOS
           ========================= */
        UnorderedList allergens = new UnorderedList();
        if (product.getAllergens() != null && !product.getAllergens().isEmpty()) {
            product.getAllergens()
                    .forEach(a -> allergens.add(new ListItem(a.getName())));
        } else {
            allergens.add(new ListItem("— Sin información —"));
        }

        content.add(
                bigImg,
                desc,
                price,
                new H3("Ingredientes"),
                ingredientsLayout,
                new H3("Personaliza tu pedido"),
                customizableLayout,
                new H3("Alérgenos"),
                allergens
        );

        dialog.add(content);

        // Botón seleccionar
        Button select = new Button("Seleccionar este producto", e -> {
            selectedProducts.put(category, product);
            dialog.close();
            Notification.show("Producto seleccionado", 1500, Notification.Position.MIDDLE);
        });
        styleButton(select);

        dialog.getFooter().add(new Button("Cerrar", e -> dialog.close()));
        dialog.open();
    }

    private void openMenuDetails(Menu menu) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(menu.getName());

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        // Imagen grande
        Image bigImg = new Image(getMenuImage(menu), menu.getName());
        bigImg.setWidth("420px");

        Paragraph desc = new Paragraph(menu.getDescription() != null ? menu.getDescription() : "");
        Paragraph price = new Paragraph("Precio: " + currency.format(menu.getPrice()));

        content.add(bigImg, desc, price);

        // ============================
        // SELECCIÓN DE PRODUCTOS POR CATEGORÍA
        // ============================

        // Estructura donde guardaremos la selección final
        Map<String, Product> selectedProducts = new HashMap<>();

        // Helper para crear selector por categoría
        BiConsumer<String, List<Product>> addCategorySelector = (label, products) -> {
            if (products == null || products.isEmpty()) return;

            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();

            int qty = switch (label) {
                case "Main" -> menu.getMainQuantity();
                case "Side" -> menu.getSideQuantity();
                case "Drink" -> menu.getDrinkQuantity();
                case "Secondary" -> menu.getSecondaryQuantity();
                case "Dessert" -> menu.getDessertQuantity();
                default -> 1;
            };

            H4 title = new H4(label + " (x" + qty + "):");
            ComboBox<Product> selector = new ComboBox<>();
            selector.setItems(products);
            selector.setItemLabelGenerator(Product::getName);

            // Si solo hay uno, seleccionarlo automáticamente
            if (products.size() == 1) {
                selector.setValue(products.get(0));
                selectedProducts.put(label, products.get(0));
            }

            selector.addValueChangeListener(ev -> {
                if (ev.getValue() != null) {
                    selectedProducts.put(label, ev.getValue());
                }
            });

            // Botón para personalizar ingredientes
            Button customize = new Button("Personalizar");
            customize.addClickListener(ev -> {
                Product p = selector.getValue();
                if (p != null) openMenuProductCardboard(p, selectedProducts, label);
            });

            row.add(title, selector, customize);
            content.add(row);
        };

        // Añadir selectores por categoría
        addCategorySelector.accept("Main", menu.getMainProducts());
        addCategorySelector.accept("Side", menu.getSideProducts());
        addCategorySelector.accept("Drink", menu.getDrinkProducts());
        addCategorySelector.accept("Secondary", menu.getSecondaryProducts());
        addCategorySelector.accept("Dessert", menu.getDessertProducts());

        // ============================
        // BOTÓN AÑADIR AL PEDIDO
        // ============================

        Button addToCart = new Button("Añadir menú personalizado");
        styleButton(addToCart);

        addToCart.addClickListener(ev -> {

            // Crear snapshot personalizado
            MenuSnapshot snapshot = new MenuSnapshot(menu);

            // Reemplazar productos por los seleccionados
            snapshot.setMainProducts(List.of(selectedProducts.get("Main")));
            snapshot.setSideProducts(List.of(selectedProducts.get("Side")));
            snapshot.setDrinkProducts(List.of(selectedProducts.get("Drink")));

            if (selectedProducts.containsKey("Secondary"))
                snapshot.setSecondaryProducts(List.of(selectedProducts.get("Secondary")));

            if (selectedProducts.containsKey("Dessert"))
                snapshot.setDessertProducts(List.of(selectedProducts.get("Dessert")));

            // Añadir al carrito
            cartService.addMenuSnapshot(snapshot);

            Notification.show("Menú personalizado añadido al pedido",
                            2500, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            dialog.close();
        });

        dialog.add(content);
        dialog.getFooter().add(addToCart, new Button("Cerrar", e -> dialog.close()));
        dialog.open();
    }

    private void openIngredientEditor(Product product,
                                      Map<String, Product> selectedProducts,
                                      String category) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Personalizar " + product.getName());

        VerticalLayout layout = new VerticalLayout();

        // Crear snapshot editable
        ProductSnapshot snapshot = new ProductSnapshot(product);

        for (var ing : snapshot.getIngredients()) {
            if (!ing.isCustomizable()) continue;

            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();

            Span label = new Span(ing.getName());

            NumberField qty = new NumberField();
            qty.setMin(0);
            qty.setMax(3);
            qty.setStep(1);
            qty.setValue((double) ing.getQuantity());
            qty.setWidth("80px");

            qty.addValueChangeListener(ev -> {
                if (ev.getValue() != null) {
                    ing.setQuantity(ev.getValue().intValue());
                }
            });

            row.add(label, qty);
            layout.add(row);
        }

        Button save = new Button("Guardar", e -> {
            // Guardar snapshot personalizado
            Product customized = snapshot.toProduct();
            selectedProducts.put(category, customized);
            dialog.close();
        });

        dialog.add(layout);
        dialog.getFooter().add(save, new Button("Cancelar", e -> dialog.close()));
        dialog.open();
    }


    /* =========================
       PRODUCTOS
       ========================= */

    private void showProductsByType(FoodType type) {
        showingFoodTypes = false;
        productGrid.removeAll();

        Button back = new Button("← Volver");
        styleButton(back, 600);
        back.addClickListener(e -> showFoodTypes());

        Div backWrapper = new Div(back);
        backWrapper.getStyle().set("width", "100%");
        productGrid.add(backWrapper);

        menuService.findActiveProducts().stream()
                .filter(p -> p.getType() != null &&
                        p.getType().getId().equals(type.getId()))
                .forEach(p -> productGrid.add(createProductCard(p)));
    }

     /* =========================
       PRODUCT CARD (SIN INGREDIENTES)
       ========================= */

    private Div createProductCard(Product product) {
        Div card = new Div();
        card.addClassName("product-card");
        card.getStyle()
                .set("padding", "14px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("text-align", "center")
                .set("max-width", "260px")
                .set("background-color", "white");

        H1 name = new H1(product.getName());
        name.getStyle()
                .set("color", "#ff7b00")
                .set("font-size", "1.35rem")
                .set("margin", "8px 0 0");

        Image img = new Image(getImageDataUrl(product), product.getName());
        img.setWidth("180px");
        img.getStyle().set("border-radius", "12px");

        Paragraph price = new Paragraph(currency.format(product.getPrice()));
        price.getStyle().set("font-weight", "bold");

        Button add = new Button("Agregar al Pedido", e -> {
            if (!isAuthenticated()) {
                UI.getCurrent().navigate("login");
                return;
            }
            cartService.addProduct(product);
            Notification n = Notification.show(
                    "Añadido al pedido", 2000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        styleButton(add);

        Button details =
                new Button("Ver detalles", e -> openDetailsDialog(product));
        styleButton(details, 600);

        card.add(img, name, price, add, details);
        return card;
    }

    /* =========================
       DETAILS DIALOG
       ========================= */

    private void openDetailsDialog(Product product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(product.getName());

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);content.setPadding(false);

        Image bigImg = new Image(getImageDataUrl(product), product.getName());
        bigImg.setWidth("420px");

        Paragraph desc = new Paragraph(
                product.getDescription() != null
                        ? product.getDescription()
                        : "Sin descripción disponible");

        Paragraph price =
                new Paragraph("Precio: " + currency.format(product.getPrice()));

        /* =========================
           INGREDIENTES NORMALES
           ========================= */
        VerticalLayout ingredientsLayout = new VerticalLayout();
        ingredientsLayout.setSpacing(false);
        ingredientsLayout.setPadding(false);

        if (product.getIngredients() != null && !product.getIngredients().isEmpty()) {
            product.getIngredients().forEach(ingredient -> {

                Div row = new Div();
                row.getStyle()
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "space-between")
                        .set("gap", "12px")
                        .set("width", "100%");

                Span name = new Span(ingredient.getName());
                name.getStyle().set("flex", "1");

                Span quantity = new Span(String.valueOf((int) ingredient.getQuantity()));
                quantity.getStyle()
                        .set("min-width", "24px")
                        .set("text-align", "center")
                        .set("font-weight", "bold");

                row.add(name, quantity);
                ingredientsLayout.add(row);
            });
        } else {
            ingredientsLayout.add(new Span("— Sin ingredientes —"));
        }

        /* =========================
           INGREDIENTES PERSONALIZABLES
           ========================= */
        VerticalLayout customizableLayout = new VerticalLayout();
        customizableLayout.setSpacing(false);
        customizableLayout.setPadding(false);

        List<Product.Ingredient> customizableIngredients =
                product.getIngredients().stream()
                        .filter(Product.Ingredient::isCustomizable)
                        .toList();

        if (customizableIngredients.isEmpty()) {
            customizableLayout.add(new Span("— No hay ingredientes personalizables —"));
        } else {
            customizableIngredients.forEach(ingredient -> {

                Div row = new Div();
                row.getStyle()
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "space-between")
                        .set("gap", "12px")
                        .set("width", "100%");

                Span name = new Span(ingredient.getName());
                name.getStyle().set("flex", "1");

                Span quantity = new Span(String.valueOf((int) ingredient.getQuantity()));
                quantity.getStyle()
                        .set("min-width", "24px")
                        .set("text-align", "center")
                        .set("font-weight", "bold");

                Button minus = new Button("−");
                minus.addClickListener(e -> {
                    int q = Integer.parseInt(quantity.getText());
                    if (q > 0) {
                        q--;
                        quantity.setText(String.valueOf(q));
                        ingredient.setQuantity(q);
                    }
                });

                Button plus = new Button("+");
                plus.addClickListener(e -> {
                    int q = Integer.parseInt(quantity.getText());
                    if (q < 3) {
                        q++;
                        quantity.setText(String.valueOf(q));
                        ingredient.setQuantity(q);
                    }
                });

                minus.getStyle().set("min-width", "32px").set("background-color", "#eee");
                plus.getStyle().set("min-width", "32px").set("background-color", "#eee");

                row.add(name, minus, quantity, plus);
                customizableLayout.add(row);
            });
        }

        /* =========================
           ALÉRGENOS
           ========================= */
        UnorderedList allergens = new UnorderedList();
        if (product.getAllergens() != null && !product.getAllergens().isEmpty()) {
            product.getAllergens()
                    .forEach(a -> allergens.add(new ListItem(a.getName())));
        } else {
            allergens.add(new ListItem("— Sin información —"));
        }

        content.add(
                bigImg,
                desc,
                price,
                new H3("Ingredientes"),
                ingredientsLayout,
                new H3("Personaliza tu pedido"),
                customizableLayout,
                new H3("Alérgenos"),
                allergens
        );

        dialog.add(content);

        Button add = new Button("Agregar al Pedido", e -> {

            // 1️⃣ Crear un clon del producto
            Product customized = new Product();
            customized.setName(product.getName());
            customized.setDescription(product.getDescription());
            customized.setPrice(product.getPrice());
            customized.setType(product.getType());
            customized.setAllergens(product.getAllergens());
            customized.setImage(product.getImage());
            customized.setActive(product.isActive());

            // 2️⃣ Clonar ingredientes con cantidades modificadas
            List<Product.Ingredient> clonedIngredients = product.getIngredients().stream()
                    .map(ing -> new Product.Ingredient(
                            ing.getName(),
                            ing.getQuantity(),      // ← cantidad modificada por el usuario
                            ing.isCustomizable()
                    ))
                    .toList();

            customized.setIngredients(clonedIngredients);

            // 3️⃣ Añadir al carrito el producto personalizado
            cartService.addProduct(customized);

            dialog.close();
            Notification n = Notification.show(
                    "Añadido al pedido", 2000, Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        styleButton(add);

        Button close = new Button("Cerrar", e -> dialog.close());
        dialog.getFooter().add(close, add);
        dialog.open();
    }

    /* =========================
       FILTER
       ========================= */

    private void openFilterMenu() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Filtrar productos");

        VerticalLayout content = new VerticalLayout();

        Select<FoodType> typeSelect = new Select<>();
        typeSelect.setLabel("Tipo");
        typeSelect.setItemLabelGenerator(FoodType::getName);
        typeSelect.setItems(menuService.findAllFoodTypes());

        Select<Allergen> allergenSelect = new Select<>();
        allergenSelect.setLabel("Alérgenos");
        allergenSelect.setItemLabelGenerator(a -> "Sin " + a.getName());
        allergenSelect.setItems(menuService.findAllAllergens());

        Button apply = new Button("Aplicar filtros", e -> {
            showingFoodTypes = false;
            productGrid.removeAll();

            List<Long> allergenIds =
                    allergenSelect.getValue() != null
                            ? List.of(allergenSelect.getValue().getId())
                            : List.of();

            List<Product> products =
                    menuService.findActiveWithoutAllergens(allergenIds);

            FoodType type = typeSelect.getValue();
            if (type != null) {
                products = products.stream()
                        .filter(p -> p.getType() != null &&
                                p.getType().getId().equals(type.getId()))
                        .toList();
            }

            products.forEach(p -> productGrid.add(createProductCard(p)));
            dialog.close();
        });

        dialog.add(content);
        dialog.getFooter().add(new Button("Cerrar", e -> dialog.close()), apply);
        dialog.open();
    }

    /* ========================= */

    private boolean isAuthenticated() {
        Authentication a =
                SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.isAuthenticated()
                && !"anonymousUser".equals(a.getPrincipal());
    }

    private String getImageDataUrl(Product p) {
        if (p.getImage() != null && p.getImage().length > 0) {
            return "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(p.getImage());
        }
        return "path/to/default/image.jpg";
    }

    private void styleButton(Button b) {
        styleButton(b, 700);
    }

    private void styleButton(Button b, int weight) {
        b.getStyle()
                .set("background-color", "#ff7b00")
                .set("color", "white")
                .set("font-weight", String.valueOf(weight))
                .set("border-radius", "8px")
                .set("width", "100%");
    }
}
