package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.CartService;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
    private final OrderService orderService;
    private final UserService userService;

    private final NumberFormat currency;

    public CarritoView(CartService cartService,
                       OrderService orderService,
                       UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;

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
        // Agrupar por ID de producto
        Map<Long, List<Product>> byId = cartItems.stream()
                .collect(Collectors.groupingBy(Product::getId, LinkedHashMap::new, Collectors.toList()));

        VerticalLayout itemsColumn = new VerticalLayout();
        itemsColumn.addClassName("cart-items-column");
        itemsColumn.setPadding(false);
        itemsColumn.setSpacing(true);
        itemsColumn.setWidth("86%");
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

        Div summary = createOrderSummary(total);
        summary.addClassName("order-summary-card");
        summary.getStyle()
                .set("margin", "8px auto 18px auto")
                .set("width", "fit-content");

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

        Button placeOrder = new Button("Realizar Pedido", e -> abrirSimuladorPaypal());

        buttons.add(continueShopping, clearCart, placeOrder);
        return buttons;
    }

    private void refreshView() {
        createView();
    }

    // ===================== Pago simulado + creación de pedido =====================

    private void abrirSimuladorPaypal() {
        if (cartService.getItemCount() <= 0) {
            Notification.show("El carrito está vacío", 2000, Notification.Position.BOTTOM_END);
            return;
        }

        BigDecimal total = calcularTotalActual();

        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Simulador PayPal");

        VerticalLayout content = new VerticalLayout(
                new H3("Total a pagar: " + currency.format(total)),
                new Paragraph("Esto es un flujo de pago simulado para el proyecto (sin cobro real).")
        );
        content.setSpacing(false);
        content.setPadding(false);

        Button cancelar = new Button("Cancelar", e -> dlg.close());
        Button confirmar = new Button("Pagar ahora", e -> {
            dlg.close();
            procesarPagoExitoso();
        });

        HorizontalLayout footer = new HorizontalLayout(cancelar, confirmar);
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dlg.add(content);
        dlg.getFooter().add(footer);
        dlg.open();
    }

    private BigDecimal calcularTotalActual() {
        return cartService.getCartItems().stream()
                .collect(Collectors.groupingBy(Product::getId))
                .entrySet()
                .stream()
                .map(e -> {
                    Product p = e.getValue().getFirst();
                    int qty = e.getValue().size();
                    return BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void procesarPagoExitoso() {
        // 1) Usuario actual
        String username = userService.getCurrentUsername();
        if (username == null) {
            Notification.show("Debes iniciar sesión para comprar");
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        User cliente = userService.findByUsername(username).orElse(null);
        if (cliente == null) {
            Notification.show("Usuario no encontrado");
            return;
        }

        // 2) Mapear líneas del carrito a OrderItem (agrupando por producto)
        Map<Product, Integer> qtyByProduct = cartService.getCartItems().stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> 1,
                        Integer::sum,
                        LinkedHashMap::new
                ));

        List<OrderItem> items = qtyByProduct.entrySet().stream()
                .map(e -> new OrderItem(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 3) Crear pedido + estado inicial
        Order order = orderService.createOrder(cliente, items);
        orderService.updateStatus(order.getId(), "EN COCINA");

        // 4) Vaciar carrito y notificar
        cartService.clearCart();
        Notification.show("¡Pedido realizado con éxito!", 3000, Notification.Position.TOP_CENTER);

        // 5) Volver a la carta (o a “mis pedidos” si luego haces esa vista)
        UI.getCurrent().navigate("carta");
    }
}
