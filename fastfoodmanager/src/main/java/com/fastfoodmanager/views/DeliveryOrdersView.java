package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
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

@PageTitle("Repartos")
@RolesAllowed({"DELIVERY","ADMIN"})
@Route(value = "delivery/orders", layout = MainLayout.class)
public class DeliveryOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public DeliveryOrdersView(OrderService orderService) {
        this.orderService = orderService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("🛵 Repartidor - Pedidos EN REPARTO");
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
                .setHeader("Creado").setAutoWidth(true);
        grid.addColumn(o -> o.getDeliveryTo() == null ? "-" : o.getDeliveryTo())
                .setHeader("Asignado a").setAutoWidth(true);

        grid.addComponentColumn(o -> {
            Button delivered = new Button("Marcar ENTREGADO", e -> {
                try {
                    String me = getCurrentUsername();
                    orderService.markDelivered(o.getId(), me);
                    Notification.show("Pedido " + o.getId() + " → ENTREGADO", 2000, Notification.Position.MIDDLE);
                    refresh();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });
            return delivered;
        }).setHeader("Acción").setAutoWidth(true);

        add(top, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        String me = getCurrentUsername();
        if (me == null) {
            grid.setItems(List.of());
            return;
        }
        List<Order> data = orderService.findForDelivery(me);
        grid.setItems(data);
        grid.getDataProvider().refreshAll();
    }

    private String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        String name = a.getName();
        return "anonymousUser".equals(name) ? null : name;
    }
}
