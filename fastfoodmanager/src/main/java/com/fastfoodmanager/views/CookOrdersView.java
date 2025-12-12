package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.service.OrderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

@PageTitle("Cocina")
@RolesAllowed({"COOK","ADMIN"})
@Route(value = "cook/orders", layout = MainLayout.class)
public class CookOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CookOrdersView(OrderService orderService) {
        this.orderService = orderService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("👨‍🍳 Cocina - Pedidos EN COCINA");
        Button refreshBtn = new Button("Refrescar", e -> refresh());

        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(e -> refresh());

        HorizontalLayout top = new HorizontalLayout(title, refreshBtn);
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCustomer() != null ? o.getCustomer().getUsername() : "-")
                .setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(o -> o.getCreatedAt() == null ? "-" : o.getCreatedAt().format(fmt))
                .setHeader("Creado").setAutoWidth(true).setSortable(true);

        grid.addColumn(o -> formatItems(o.getItems()))
                .setHeader("Productos").setAutoWidth(false).setFlexGrow(1);

        grid.addComponentColumn(o -> {
            Button done = new Button("HECHO → LISTO", e -> {
                String cook = getCurrentUsername();
                try {
                    orderService.markCookedDone(o.getId(), cook != null ? cook : "cocinero");
                    Notification.show("Pedido " + o.getId() + " → LISTO", 2000, Notification.Position.MIDDLE);
                    refresh();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });
            return done;
        }).setHeader("Acción").setAutoWidth(true);

        add(top, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        List<Order> data = orderService.findForCook();
        grid.setItems(data);
        grid.getDataProvider().refreshAll();
    }

    private String formatItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "-";
        return items.stream()
                .map(i -> (i.getProduct() != null ? i.getProduct().getName() : "Producto")
                        + " x" + i.getQuantity())
                .reduce((a, b) -> a + ", " + b)
                .orElse("-");
    }

    private String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        String name = a.getName();
        return "anonymousUser".equals(name) ? null : name;
    }
}
