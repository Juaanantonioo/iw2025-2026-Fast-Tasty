package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.ProductService;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;

import java.util.*;

@PageTitle("Pedido | Cliente")
@RolesAllowed("USER")
@Route(value = "client/order", layout = MainLayout.class)
public class ClientOrderView extends VerticalLayout {

    private static final String CART_KEY = "CART_MAP";

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    private final Grid<Row> grid = new Grid<>(Row.class, false);

    public ClientOrderView(ProductService productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        add(new H1("Carrito"));

        grid.addColumn(Row::name).setHeader("Producto").setFlexGrow(1);
        grid.addColumn(r -> String.valueOf(r.qty())).setHeader("Cantidad").setAutoWidth(true);
        grid.addColumn(r -> String.format("€ %.2f", r.unitPrice())).setHeader("Precio").setAutoWidth(true);

        grid.addComponentColumn(r -> new Button("Eliminar", e -> {
            removeProduct(r.productId());
            refresh();
        })).setHeader("Acción").setAutoWidth(true);

        Button pay = new Button("Pagar", e -> pay());
        pay.getStyle()
                .set("background", "#0070ba")
                .set("color", "white")
                .set("font-weight", "700")
                .set("border-radius", "10px")
                .set("padding", "10px 18px");

        add(grid, pay);
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        Map<Long, Integer> cart = getCart();
        List<Product> all = productService.findAll();

        List<Row> rows = new ArrayList<>();
        for (var entry : cart.entrySet()) {
            Long pid = entry.getKey();
            int qty = entry.getValue();
            Product p = all.stream().filter(x -> Objects.equals(x.getId(), pid)).findFirst().orElse(null);
            if (p != null) rows.add(new Row(pid, p.getName(), qty, p.getPrice() == null ? 0.0 : p.getPrice()));
        }

        grid.setItems(rows);
        grid.getDataProvider().refreshAll();
    }

    private void removeProduct(Long productId) {
        Map<Long, Integer> cart = getCart();
        cart.remove(productId);
        VaadinSession.getCurrent().setAttribute(CART_KEY, cart);
    }

    private void pay() {
        Map<Long, Integer> cart = getCart();
        if (cart.isEmpty()) {
            Notification.show("El carrito está vacío", 2000, Notification.Position.MIDDLE);
            return;
        }

        String username = userService.getCurrentUsername();
        if (username == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            Notification.show("No se pudo obtener el usuario", 2500, Notification.Position.MIDDLE);
            return;
        }

        // Convertir cart (ids) -> OrderItems (entidades)
        List<Product> all = productService.findAll();
        List<OrderItem> items = new ArrayList<>();
        for (var entry : cart.entrySet()) {
            Long pid = entry.getKey();
            int qty = entry.getValue();
            Product p = all.stream().filter(x -> Objects.equals(x.getId(), pid)).findFirst().orElse(null);
            if (p != null) items.add(new OrderItem(p, qty));
        }

        if (items.isEmpty()) {
            Notification.show("No hay productos válidos en el carrito", 2500, Notification.Position.MIDDLE);
            return;
        }

        Order order = orderService.createOrder(user, items);
        orderService.markAsPaid(order.getId());

        // vaciar carrito
        cart.clear();
        VaadinSession.getCurrent().setAttribute(CART_KEY, cart);

        Notification.show("Pedido #" + order.getId() + " pagado y registrado.", 2500, Notification.Position.MIDDLE);
        UI.getCurrent().navigate("client/orders");
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

    private record Row(Long productId, String name, int qty, double unitPrice) {}
}
