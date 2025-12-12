package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.CartService;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@PageTitle("Pedido | FastTasty")
@RolesAllowed("USER")
@Route(value = "carrito", layout = MainLayout.class)
public class CarritoView extends VerticalLayout {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    private final Grid<OrderItem> grid = new Grid<>(OrderItem.class, false);
    private final Span totalLabel = new Span();

    public CarritoView(CartService cartService, OrderService orderService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("🛒 Tu pedido");

        grid.addColumn(i -> i.getProduct() != null ? i.getProduct().getName() : "-")
                .setHeader("Producto").setFlexGrow(1);
        grid.addColumn(i -> String.format("€ %.2f", i.getUnitPrice()))
                .setHeader("Precio").setAutoWidth(true);
        grid.addColumn(OrderItem::getQuantity)
                .setHeader("Cantidad").setAutoWidth(true);
        grid.addColumn(i -> String.format("€ %.2f", i.getSubtotal()))
                .setHeader("Subtotal").setAutoWidth(true);

        grid.addComponentColumn(i -> {
            Button minus = new Button("−", e -> { cartService.decrement(i); refresh(); });
            Button plus = new Button("+", e -> { cartService.add(i.getProduct()); refresh(); });
            Button del = new Button("Eliminar", e -> { cartService.remove(i); refresh(); });
            return new HorizontalLayout(minus, plus, del);
        }).setHeader("Acciones").setAutoWidth(true);

        Button backToCarta = new Button("← Volver a la carta", e -> UI.getCurrent().navigate("carta"));

        Button pay = new Button("💳 Pagar con PayPal", e -> pay());
        pay.getStyle().set("background-color", "#0070ba").set("color", "white").set("font-weight", "600");

        totalLabel.getStyle().set("font-weight", "700");

        add(title, grid, totalLabel, new HorizontalLayout(backToCarta, pay));
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        List<OrderItem> items = cartService.getItems();
        grid.setItems(items);
        totalLabel.setText("Total: " + String.format("€ %.2f", cartService.total()) +
                " | Artículos: " + cartService.countItems());
        grid.getDataProvider().refreshAll();
    }

    private void pay() {
        if (cartService.getItems().isEmpty()) {
            Notification.show("Tu carrito está vacío");
            return;
        }

        String username = userService.getCurrentUsername();
        if (username == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        Order order = orderService.createOrder(user, cartService.getItemsCopy());
        orderService.markAsPaid(order.getId());

        cartService.clear();
        refresh();

        Notification.show("✅ Pedido #" + order.getId() + " creado (ENVIADO).");
        UI.getCurrent().navigate("client/orders");
    }
}
