package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.*;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.ProductService;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Pedido | Cliente")
@RolesAllowed("USER")
@Route(value = "client/order", layout = MainLayout.class)
public class ClientOrderView extends VerticalLayout {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    private final List<OrderItem> carrito = new ArrayList<>();
    private final Grid<OrderItem> grid = new Grid<>(OrderItem.class, false);
    private final Span totalLabel = new Span("Total: 0 €");

    public ClientOrderView(ProductService productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;

        add(new H2("🛒 Tu pedido"));

        // Mostrar productos disponibles
        Grid<Product> productosGrid = new Grid<>(Product.class, false);
        productosGrid.addColumn(Product::getName).setHeader("Producto");
        productosGrid.addColumn(Product::getPrice).setHeader("Precio (€)");
        productosGrid.addComponentColumn(p -> new Button("Añadir", e -> addToCart(p)));
        productosGrid.setItems(productService.findAll());

        grid.addColumn(i -> i.getProduct().getName()).setHeader("Producto");
        grid.addColumn(OrderItem::getQuantity).setHeader("Cantidad");
        grid.addComponentColumn(i -> new Button("Eliminar", e -> removeFromCart(i)));

        Button pagar = new Button("💳 Pagar con PayPal (Simulado)", e -> simulatePayPalPayment());

        pagar.getStyle()
                .set("background-color", "#0070ba")
                .set("color", "white")
                .set("font-weight", "600");

        add(productosGrid, new H2("🧾 Carrito"), grid, totalLabel, pagar);
    }

    private void addToCart(Product p) {
        carrito.stream()
                .filter(i -> i.getProduct().getId().equals(p.getId()))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.setQuantity(i.getQuantity() + 1),
                        () -> carrito.add(new OrderItem(p, 1))
                );
        refreshCart();
    }

    private void removeFromCart(OrderItem item) {
        carrito.remove(item);
        refreshCart();
    }

    private void refreshCart() {
        grid.setItems(carrito);
        double total = carrito.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
        totalLabel.setText("Total: " + String.format("%.2f", total) + " €");
    }

    private void simulatePayPalPayment() {
        if (carrito.isEmpty()) {
            Notification.show("Tu carrito está vacío");
            return;
        }

        User user = userService.findByUsername(userService.getCurrentUsername()).orElse(null);
        if (user == null) {
            Notification.show("Debes iniciar sesión para realizar un pedido");
            UI.getCurrent().navigate("login");
            return;
        }

        Order order = orderService.createOrder(user, carrito);
        orderService.markAsPaid(order.getId());

        carrito.clear();
        refreshCart();
        Notification.show("✅ Pago simulado completado. Pedido #" + order.getId() + " registrado.");
        UI.getCurrent().navigate("operator/orders");
    }
}
