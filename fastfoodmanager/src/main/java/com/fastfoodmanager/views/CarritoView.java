package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.CartService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Carrito | FastTasty")
@Route(value = "carrito", layout = MainLayout.class)
@CssImport("./themes/my-theme/home.css")
@RolesAllowed({"USER", "ADMIN"})
public class CarritoView extends VerticalLayout {

    private final CartService cartService;
    private final NumberFormat currency;

    public CarritoView(CartService cartService) {
        this.cartService = cartService;
        this.currency = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        addClassName("carrito-view");
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        createView();
    }

    private void createView() {
        removeAll();
        createHeader();
        createContent();
    }

    private void createHeader() {
        // Barra superior con “Volver”
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        Button backButton = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        backButton.setText("Volver a la Carta");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("carta")));
        topBar.add(backButton);

        // Título centrado
        H1 title = new H1("Tu Pedido");
        title.addClassName("page-title");
        title.getStyle().set("margin-bottom", "10px");

        add(topBar, title);
    }

    private void createContent() {
        List<Product> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            showCartItems(cartItems);
        }
    }

    private void showEmptyCart() {
        Div emptyState = new Div();
        emptyState.addClassName("empty-state");

        Icon cartIcon = VaadinIcon.CART.create();
        cartIcon.addClassName("empty-cart-icon");

        H2 emptyTitle = new H2("Tu carrito está vacío");
        Paragraph emptyMessage = new Paragraph("¡Descubre nuestros deliciosos platos y añade algunos a tu pedido!");

        Button browseButton = new Button("Ver la Carta",
                e -> getUI().ifPresent(ui -> ui.navigate("carta")));

        emptyState.add(cartIcon, emptyTitle, emptyMessage, browseButton);
        add(emptyState);
    }

    private void showCartItems(List<Product> cartItems) {
        // === Agrupar por ID de producto ===
        Map<Long, List<Product>> byId = cartItems.stream()
                .collect(Collectors.groupingBy(Product::getId, LinkedHashMap::new, Collectors.toList()));

        VerticalLayout itemsColumn = new VerticalLayout();
        itemsColumn.addClassName("cart-items-column");
        itemsColumn.setPadding(false);
        itemsColumn.setSpacing(true);
        itemsColumn.setWidth("86%"); // ancho cómodo en centro
        itemsColumn.getStyle().set("margin", "0 auto");

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, List<Product>> entry : byId.entrySet()) {
            List<Product> group = entry.getValue();
            Product product = group.getFirst();
            long quantity = group.size();

            BigDecimal subtotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(quantity));
            total = total.add(subtotal);

            itemsColumn.add(createCartItemLayout(product, quantity, subtotal));
        }

        // === RESUMEN CENTRADO ARRIBA ===
        Div summary = createOrderSummary(total);
        summary.addClassName("order-summary-card");
        summary.getStyle()
                .set("margin", "8px auto 18px auto")
                .set("width", "fit-content");

        // Botones debajo
        HorizontalLayout actionButtons = createActionButtons();

        add(summary, itemsColumn, actionButtons);
    }

    private HorizontalLayout createCartItemLayout(Product product, long quantity, BigDecimal subtotal) {
        HorizontalLayout itemLayout = new HorizontalLayout();
        itemLayout.addClassName("cart-item-card");
        itemLayout.setWidthFull();
        itemLayout.setAlignItems(Alignment.CENTER);
        itemLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Div productInfo = new Div();
        productInfo.addClassName("product-info");

        H2 productName = new H2(product.getName());
        productName.addClassName("product-name");

        Paragraph productDesc = new Paragraph(product.getDescription());
        productDesc.addClassName("product-desc");

        productInfo.add(productName, productDesc);

        Div quantityInfo = new Div();
        quantityInfo.addClassName("quantity-info");
        quantityInfo.setText("Cantidad: " + quantity);

        Div priceInfo = new Div();
        priceInfo.addClassName("price-info");
        priceInfo.setText(currency.format(subtotal));

        Button removeButton = new Button("-", e -> {
            cartService.removeProduct(product);  // quita UNA unidad
            refreshView();
        });

        Button addButton = new Button("+", e -> {
            cartService.addProduct(product);
            refreshView();
        });

        HorizontalLayout buttons = new HorizontalLayout(removeButton, addButton);
        buttons.setSpacing(true);

        itemLayout.add(productInfo, quantityInfo, priceInfo, buttons);
        itemLayout.expand(productInfo);

        return itemLayout;
    }

    private Div createOrderSummary(BigDecimal total) {
        Div summary = new Div();
        summary.addClassName("order-summary");

        H2 summaryTitle = new H2("Resumen del Pedido");
        Paragraph totalText = new Paragraph("Total: " + currency.format(total));
        totalText.addClassName("total-price");
        totalText.getStyle().set("font-size", "1.6rem")
                .set("font-weight", "800")
                .set("margin-top", "6px");

        summary.add(summaryTitle, totalText);
        return summary;
    }

    private HorizontalLayout createActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addClassName("action-buttons");
        buttons.setWidthFull();
        buttons.setJustifyContentMode(JustifyContentMode.END);

        Button continueShopping = new Button("Seguir Comprando",
                e -> getUI().ifPresent(ui -> ui.navigate("carta")));

        Button clearCart = new Button("Vaciar Carrito", e -> {
            cartService.clearCart();
            Notification.show("Carrito vaciado", 2000, Notification.Position.BOTTOM_END);
            refreshView();
        });

        Button placeOrder = new Button("Realizar Pedido", e -> {
            if (cartService.getItemCount() > 0) {
                Notification.show("¡Pedido realizado con éxito!", 3000, Notification.Position.MIDDLE);
                cartService.clearCart();
                getUI().ifPresent(ui -> ui.navigate("carta"));
            } else {
                Notification.show("El carrito está vacío", 2000, Notification.Position.BOTTOM_END);
            }
        });

        buttons.add(continueShopping, clearCart, placeOrder);
        return buttons;
    }

    private void refreshView() {
        createView();
    }
}
