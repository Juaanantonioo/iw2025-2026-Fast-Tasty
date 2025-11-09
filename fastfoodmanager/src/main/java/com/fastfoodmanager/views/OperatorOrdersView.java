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

import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@PageTitle("Pedidos")
@RolesAllowed({"OPERATOR","ADMIN"})
@Route(value = "operator/orders", layout = MainLayout.class)
public class OperatorOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);

    private static final List<String> WORKFLOW = List.of(
            "EN COCINA", "PREPARANDO", "LISTO", "EN REPARTO", "ENTREGADO"
    );

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OperatorOrdersView(OrderService orderService) {
        this.orderService = orderService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        var title = new H1("📦 Pedidos asignados");
        var refreshBtn = new Button("Refrescar", e -> refresh());

        // Autorefresco cada 5s
        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(e -> refresh());

        var top = new HorizontalLayout(title, refreshBtn);
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        // ----- Columnas -----
        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCustomer() != null ? o.getCustomer().getUsername() : "-")
                .setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(o -> String.format("€ %.2f", o.getTotal() == null ? 0.0 : o.getTotal()))
                .setHeader("Total").setAutoWidth(true);
        grid.addColumn(o -> o.getStatus() == null ? "-" : o.getStatus())
                .setHeader("Estado").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCreatedAt() == null ? "-" : o.getCreatedAt().format(fmt))
                .setHeader("Creado").setAutoWidth(true).setSortable(true);

        // Acción: siguiente estado
        grid.addComponentColumn(o -> {
            String next = nextStatus(o.getStatus());
            Button b = new Button(next == null ? "—" : ("A " + next), e -> {
                if (next != null) {
                    orderService.updateStatus(o.getId(), next);
                    Notification.show("Pedido " + o.getId() + " → " + next, 2000, Notification.Position.MIDDLE);
                    refresh();
                }
            });
            b.setEnabled(next != null);
            return b;
        }).setHeader("Avanzar").setAutoWidth(true);

        add(top, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    /** Calcula el siguiente estado del flujo predefinido. */
    private String nextStatus(String current) {
        if (current == null) return WORKFLOW.get(0);
        int idx = WORKFLOW.indexOf(current);
        if (idx < 0 || idx == WORKFLOW.size() - 1) return null; // ya en último
        return WORKFLOW.get(idx + 1);
    }

    /** Carga pedidos de BD (pendientes/no entregados) y los pinta. */
    private void refresh() {
        List<Order> data;
        try {
            // preferente: sólo los no ENTREGADO
            data = orderService.findPending();
        } catch (Throwable ignored) {
            // fallback si tu servicio aún no tiene findPending()
            data = orderService.findAll().stream()
                    .filter(o -> !Objects.equals(o.getStatus(), "ENTREGADO"))
                    .toList();
        }
        grid.setItems(data);
        grid.getDataProvider().refreshAll();
    }
}
