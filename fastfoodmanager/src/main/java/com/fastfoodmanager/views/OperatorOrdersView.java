package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderType;
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

@PageTitle("Pedidos")
@RolesAllowed({"OPERATOR","ADMIN"})
@Route(value = "operator/orders", layout = MainLayout.class)
public class OperatorOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OperatorOrdersView(OrderService orderService) {
        this.orderService = orderService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        var title = new H1("📦 Operario - Pedidos");
        var refreshBtn = new Button("Refrescar", e -> refresh());

        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(e -> refresh());

        var top = new HorizontalLayout(title, refreshBtn);
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        // Configurar columnas de la tabla
        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCustomer() != null ? o.getCustomer().getUsername() : "-")
                .setHeader("Cliente").setAutoWidth(true);

        // Mostrar tipo de pedido
        grid.addColumn(o -> o.getOrderType() != null ? o.getOrderType().getDisplayName() : "-")
                .setHeader("Tipo").setAutoWidth(true);

        // Mostrar dirección si es delivery
        grid.addColumn(o -> {
            if (o.getOrderType() == OrderType.DELIVERY && o.getDeliveryAddress() != null) {
                return o.getDeliveryAddress();
            }
            return o.getOrderType() == OrderType.PICKUP ? "Recoger en local" : "-";
        }).setHeader("Dirección/Ubicación").setWidth("200px");

        grid.addColumn(o -> String.format("€ %.2f", o.getTotal() == null ? 0.0 : o.getTotal()))
                .setHeader("Total").setAutoWidth(true);
        grid.addColumn(o -> o.getStatus() == null ? "-" : o.getStatus())
                .setHeader("Estado").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCreatedAt() == null ? "-" : o.getCreatedAt().format(fmt))
                .setHeader("Creado").setAutoWidth(true).setSortable(true);

        grid.addColumn(o -> o.getAssignedTo() == null ? "-" : o.getAssignedTo())
                .setHeader("Operario").setAutoWidth(true);

        grid.addColumn(o -> o.getDeliveryTo() == null ? "-" : o.getDeliveryTo())
                .setHeader("Repartidor").setAutoWidth(true);

        // Columna de acciones
        grid.addComponentColumn(o -> buildActionButton(o))
                .setHeader("Acción").setAutoWidth(true);

        add(top, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    private Button buildActionButton(Order o) {
        String me = getCurrentUsername();

        // ENVIADO: puedo tomarlo y mandarlo a cocina
        if ("ENVIADO".equalsIgnoreCase(o.getStatus())) {
            Button b = new Button("Mandar a cocina", e -> {
                try {
                    orderService.sendToKitchen(o.getId(), me);
                    Notification.show("Pedido " + o.getId() + " → EN COCINA", 2000, Notification.Position.MIDDLE);
                    refresh();
                } catch (Exception ex) {
                    Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });
            return b;
        }

        // LISTO: acciones diferentes según el tipo de pedido
        if ("LISTO".equalsIgnoreCase(o.getStatus())) {
            // Verificar que el operario asignado sea el actual
            boolean isAssignedToMe = me != null && me.equals(o.getAssignedTo());

            if (o.getOrderType() == OrderType.DELIVERY) {
                // Para DOMICILIO: asignar repartidor
                Button b = new Button("Asignar repartidor", e -> {
                    try {
                        orderService.assignFreeDeliveryAndSend(o.getId(), me);
                        Notification.show("Pedido " + o.getId() + " → EN REPARTO", 2000, Notification.Position.MIDDLE);
                        refresh();
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    }
                });
                b.setEnabled(isAssignedToMe);
                b.getStyle().set("background-color", "#28a745").set("color", "white");
                return b;
            } else if (o.getOrderType() == OrderType.PICKUP) {
                // Para RECOGER: marcar como recogido
                Button b = new Button("Marcar como recogido", e -> {
                    try {
                        orderService.markAsPickedUp(o.getId(), me);
                        Notification.show("Pedido " + o.getId() + " → RECOGIDO", 2000, Notification.Position.MIDDLE);
                        refresh();
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    }
                });
                b.setEnabled(isAssignedToMe);
                b.getStyle().set("background-color", "#0070ba").set("color", "white");
                return b;
            }
        }

        // Otros estados: sin acción
        Button none = new Button("—");
        none.setEnabled(false);
        return none;
    }

    private void refresh() {
        String operator = getCurrentUsername();
        if (operator == null) {
            grid.setItems(List.of());
            return;
        }
        List<Order> data = orderService.findForOperatorInbox(operator);
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