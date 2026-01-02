package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderType;
import com.fastfoodmanager.service.OrderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Pedidos | Operario")
@RolesAllowed({"OPERATOR","ADMIN"})
@Route(value = "operator/orders", layout = MainLayout.class)
public class OperatorOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OperatorOrdersView(OrderService orderService) {
        this.orderService = orderService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("dashboard-bg");

        Div page = new Div();
        page.addClassName("ft-page");

        Div header = new Div();
        header.addClassName("ft-topbar");

        Div headerBlock = new Div();
        H1 title = new H1("Operario · Pedidos");
        title.addClassName("ft-title");
        Paragraph subtitle = new Paragraph("Bandeja de pedidos y gestión de estados.");
        subtitle.addClassName("ft-subtitle");
        headerBlock.add(title, subtitle);

        Div actions = new Div();
        actions.addClassName("ft-actions");
        Button refreshBtn = new Button("Refrescar", e -> refresh());
        refreshBtn.getElement().getThemeList().add("primary");
        actions.add(refreshBtn);

        header.add(headerBlock, actions);

        Div gridCard = new Div();
        gridCard.addClassName("ft-card");

        grid.setWidthFull();

        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCustomer() != null ? o.getCustomer().getUsername() : "-")
                .setHeader("Cliente").setAutoWidth(true);

        grid.addColumn(o -> o.getOrderType() != null ? o.getOrderType().getDisplayName() : "-")
                .setHeader("Tipo").setAutoWidth(true);

        grid.addColumn(o -> {
            if (o.getOrderType() == OrderType.DELIVERY && o.getDeliveryAddress() != null) return o.getDeliveryAddress();
            return o.getOrderType() == OrderType.PICKUP ? "Recoger en local" : "-";
        }).setHeader("Dirección/Ubicación").setWidth("260px").setFlexGrow(0);

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

        grid.addComponentColumn(this::buildActionButton)
                .setHeader("Acción").setAutoWidth(true).setFlexGrow(0);

        gridCard.add(grid);

        page.add(header, gridCard);
        add(page);

        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(e -> refresh());

        refresh();
    }

    private Button buildActionButton(Order o) {
        String me = getCurrentUsername();

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
            b.getElement().getThemeList().add("primary");
            return b;
        }

        if ("LISTO".equalsIgnoreCase(o.getStatus())) {
            boolean isAssignedToMe = me != null && me.equals(o.getAssignedTo());

            if (o.getOrderType() == OrderType.DELIVERY) {
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
                b.getElement().getThemeList().add("success");
                return b;
            }

            if (o.getOrderType() == OrderType.PICKUP) {
                Button b = new Button("Marcar recogido", e -> {
                    try {
                        orderService.markAsPickedUp(o.getId(), me);
                        Notification.show("Pedido " + o.getId() + " → RECOGIDO", 2000, Notification.Position.MIDDLE);
                        refresh();
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    }
                });
                b.setEnabled(isAssignedToMe);
                b.getElement().getThemeList().add("primary");
                return b;
            }
        }

        Button none = new Button("—");
        none.setEnabled(false);
        return none;
    }

    private void refresh() {
        String operator = getCurrentUsername();
        grid.setItems(operator == null ? List.of() : orderService.findForOperatorInbox(operator));
        grid.getDataProvider().refreshAll();
    }

    private String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        String name = a.getName();
        return "anonymousUser".equals(name) ? null : name;
    }
}