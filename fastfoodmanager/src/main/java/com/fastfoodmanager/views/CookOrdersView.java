package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.service.OrderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        top.setAlignItems(FlexComponent.Alignment.CENTER);

        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCustomer() != null ? o.getCustomer().getUsername() : "-")
                .setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(o -> o.getCreatedAt() == null ? "-" : o.getCreatedAt().format(fmt))
                .setHeader("Creado").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getOrderType().toString())
                .setHeader("Tipo").setAutoWidth(true);

        grid.addColumn(o -> formatItemsPreview(o.getItems()))
                .setHeader("Productos").setAutoWidth(false).setFlexGrow(1);

        // Columna para botón de detalles
        grid.addComponentColumn(order -> {
            Button detailBtn = new Button("Ver Detalle", new Icon(VaadinIcon.EYE));
            detailBtn.addClickListener(e -> showOrderDetails(order));
            return detailBtn;
        }).setHeader("Detalles").setAutoWidth(true);

        // Columna para botón de marcar como hecho
        grid.addComponentColumn(order -> {
            Button doneBtn = new Button("HECHO → LISTO", new Icon(VaadinIcon.CHECK));
            doneBtn.addClickListener(e -> markAsDone(order));
            doneBtn.setThemeName("primary success");
            return doneBtn;
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

    private String formatItemsPreview(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "-";
        if (items.size() <= 2) {
            return items.stream()
                    .map(i -> (i.getProduct() != null ? i.getProduct().getName() : "Producto")
                            + " x" + i.getQuantity())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("-");
        } else {
            // Mostrar solo los primeros 2 productos y "..."
            String firstTwo = items.stream()
                    .limit(2)
                    .map(i -> (i.getProduct() != null ? i.getProduct().getName() : "Producto")
                            + " x" + i.getQuantity())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("-");
            return firstTwo + " y " + (items.size() - 2) + " más...";
        }
    }

    private void showOrderDetails(Order order) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setMaxWidth("90vw");

        // Cabecera del diálogo
        H2 dialogTitle = new H2("📋 Detalle del Pedido #" + order.getId());
        Span orderInfo = new Span("Cliente: " +
                (order.getCustomer() != null ? order.getCustomer().getUsername() : "Sin cliente") +
                " | " + order.getOrderType() +
                " | " + order.getCreatedAt().format(fmt));

        // Dirección si es delivery
        if (order.getOrderType().toString().equals("DELIVERY") && order.getDeliveryAddress() != null) {
            Span address = new Span("📍 Dirección: " + order.getDeliveryAddress());
            address.getStyle().set("font-size", "small");
            orderInfo.add(new Span(" | "), address);
        }

        // Lista de productos
        VerticalLayout productsLayout = new VerticalLayout();
        productsLayout.setPadding(false);
        productsLayout.setSpacing(false);

        H3 productsTitle = new H3("Productos:");
        productsLayout.add(productsTitle);

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            double total = 0;
            for (OrderItem item : order.getItems()) {
                HorizontalLayout itemLayout = new HorizontalLayout();
                itemLayout.setWidthFull();
                itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);

                Span name = new Span(item.getProduct() != null ?
                        item.getProduct().getName() : "Producto");
                name.getStyle().set("font-weight", "bold");

                Span quantity = new Span(" x" + item.getQuantity());
                quantity.getStyle().set("color", "var(--lumo-primary-text-color)");

                Span price = new Span(String.format(" - $%.2f c/u", item.getUnitPrice()));
                price.getStyle().set("font-size", "small");

                Span subtotal = new Span(String.format(" = $%.2f",
                        item.getUnitPrice() * item.getQuantity()));
                subtotal.getStyle()
                        .set("font-weight", "bold")
                        .set("margin-left", "auto");

                itemLayout.add(name, quantity, price, subtotal);
                productsLayout.add(itemLayout);

                total += item.getUnitPrice() * item.getQuantity();
            }

            // Total
            HorizontalLayout totalLayout = new HorizontalLayout();
            totalLayout.setWidthFull();
            totalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

            Span totalLabel = new Span("TOTAL:");
            totalLabel.getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "larger");

            Span totalValue = new Span(String.format(" $%.2f", total));
            totalValue.getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "larger")
                    .set("color", "var(--lumo-primary-color)");

            totalLayout.add(totalLabel, totalValue);
            productsLayout.add(totalLayout);
        } else {
            productsLayout.add(new Span("No hay productos en este pedido"));
        }

        // Observaciones o notas especiales (si tuvieras el campo)
        if (order.getDeliveryAddress() != null && order.getOrderType().toString().equals("DELIVERY")) {
            TextArea notes = new TextArea("Notas de entrega");
            notes.setValue(order.getDeliveryAddress());
            notes.setReadOnly(true);
            notes.setWidthFull();
            productsLayout.add(notes);
        }

        // Botones del diálogo
        Button closeBtn = new Button("Cerrar", e -> dialog.close());
        Button markAsDoneBtn = new Button("Marcar como LISTO", e -> {
            markAsDone(order);
            dialog.close();
        });
        markAsDoneBtn.setThemeName("primary success");

        HorizontalLayout buttons = new HorizontalLayout(closeBtn, markAsDoneBtn);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setSpacing(true);

        // Layout principal del diálogo
        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, orderInfo,
                productsLayout, buttons);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void markAsDone(Order order) {
        String cook = getCurrentUsername();
        try {
            orderService.markCookedDone(order.getId(), cook != null ? cook : "cocinero");
            Notification.show("✅ Pedido #" + order.getId() + " marcado como LISTO",
                    2000, Notification.Position.MIDDLE);
            refresh();
        } catch (Exception ex) {
            Notification.show("❌ Error: " + ex.getMessage(),
                    3000, Notification.Position.MIDDLE);
        }
    }

    private String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        String name = a.getName();
        return "anonymousUser".equals(name) ? null : name;
    }
}