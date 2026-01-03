package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.service.OrderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.fastfoodmanager.service.EmailService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Repartos")
@RolesAllowed({"DELIVERY","ADMIN"})
@Route(value = "delivery/orders", layout = MainLayout.class)
public class DeliveryOrdersView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(DeliveryOrdersView.class);

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final EmailService emailService;

    public DeliveryOrdersView(OrderService orderService, EmailService emailService) {
        this.orderService = orderService;
        this.emailService = emailService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("🛵 Repartidor - Pedidos EN REPARTO");
        Button refreshBtn = new Button("Refrescar", e -> refresh());

        // Configurar polling
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.setPollInterval(5000);
            ui.addPollListener(e -> refresh());
        }

        HorizontalLayout top = new HorizontalLayout(title, refreshBtn);
        top.setWidthFull();
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        top.setAlignItems(FlexComponent.Alignment.CENTER);

        // Configurar columnas SIMPLIFICADAS - sin acceso a relaciones complejas
        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> {
            try {
                // Solo mostrar ID del cliente para evitar LazyInitialization
                return o.getCustomer() != null ? "Cliente #" + (o.getCustomer().getId() != null ? o.getCustomer().getId() : "?") : "-";
            } catch (Exception e) {
                return "-";
            }
        }).setHeader("Cliente").setAutoWidth(true);

        grid.addColumn(o -> o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "-")
                .setHeader("Creado").setAutoWidth(true);
        grid.addColumn(o -> o.getDeliveryTo() != null ? o.getDeliveryTo() : "-")
                .setHeader("Asignado a").setAutoWidth(true);

        // Vista previa SIMPLIFICADA de productos
        grid.addColumn(o -> {
            try {
                // Solo mostrar conteo de items
                if (o.getItems() == null) return "0 productos";
                return o.getItems().size() + " producto(s)";
            } catch (Exception e) {
                return "? productos";
            }
        }).setHeader("Productos").setAutoWidth(true);

        // Columna para botón de detalles
        grid.addComponentColumn(o -> {
            Button detailBtn = new Button("Ver Detalle", new Icon(VaadinIcon.EYE));
            detailBtn.addClickListener(e -> {
                try {
                    showOrderDetails(o.getId());
                } catch (Exception ex) {
                    Notification.show("Error al cargar detalles: " + ex.getMessage(),
                            3000, Notification.Position.MIDDLE);
                }
            });
            detailBtn.setThemeName("tertiary");
            return detailBtn;
        }).setHeader("Detalles").setAutoWidth(true);

        // Columna para botón de marcar como entregado
        grid.addComponentColumn(o -> {
            Button deliveredBtn = new Button("Marcar ENTREGADO", new Icon(VaadinIcon.CHECK));
            deliveredBtn.addClickListener(e -> markAsDelivered(o));
            deliveredBtn.setThemeName("primary success");
            return deliveredBtn;
        }).setHeader("Acción").setAutoWidth(true);

        add(top, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        try {
            String me = getCurrentUsername();
            if (me == null) {
                grid.setItems(List.of());
                return;
            }
            // Usar el nuevo método que carga los pedidos con detalles
            List<Order> data = orderService.findForDeliveryWithDetails(me);
            grid.setItems(data);
        } catch (Exception e) {
            log.error("Error al refrescar datos", e);
            Notification.show("Error al cargar pedidos: " + e.getMessage(),
                    3000, Notification.Position.MIDDLE);
            grid.setItems(List.of());
        }
    }

    private void showOrderDetails(Long orderId) {
        try {
            String me = getCurrentUsername();
            if (me == null) {
                Notification.show("Error: No se pudo identificar al repartidor",
                        3000, Notification.Position.MIDDLE);
                return;
            }

            // Usar el nuevo método que carga TODO en una transacción
            Order order = orderService.findDeliveryOrderWithDetails(orderId, me);

            if (order == null) {
                Notification.show("Error: Pedido no encontrado",
                        3000, Notification.Position.MIDDLE);
                return;
            }

            Dialog dialog = new Dialog();
            dialog.setWidth("700px");
            dialog.setMaxWidth("95vw");

            // Cabecera del diálogo
            H2 dialogTitle = new H2("📦 Pedido #" + order.getId());
            dialogTitle.getStyle()
                    .set("margin-top", "0")
                    .set("color", "var(--lumo-primary-color)");

            // Información básica del pedido
            VerticalLayout orderInfo = new VerticalLayout();
            orderInfo.setPadding(false);
            orderInfo.setSpacing(false);

            String customerName = "Cliente no disponible";
            if (order.getCustomer() != null) {
                customerName = order.getCustomer().getUsername() != null ?
                        order.getCustomer().getUsername() : "Cliente #" + order.getCustomer().getId();
            }

            Span customerSpan = new Span("👤 Cliente: " + customerName);
            customerSpan.getStyle()
                    .set("font-weight", "500")
                    .set("margin-bottom", "5px");

            String orderType = order.getOrderType() != null ?
                    order.getOrderType().toString() : "PICKUP";
            Span typeSpan = new Span("📋 Tipo: " + orderType);
            typeSpan.getStyle()
                    .set("font-weight", "500")
                    .set("margin-bottom", "5px");

            String createdAt = order.getCreatedAt() != null ?
                    order.getCreatedAt().format(fmt) : "-";
            Span dateSpan = new Span("📅 Fecha: " + createdAt);
            dateSpan.getStyle().set("font-weight", "500");

            orderInfo.add(customerSpan, typeSpan, dateSpan);

            // Información de reparto
            VerticalLayout deliveryInfo = new VerticalLayout();
            deliveryInfo.setPadding(false);
            deliveryInfo.setSpacing(true);
            deliveryInfo.getStyle()
                    .set("margin-top", "15px")
                    .set("padding", "15px")
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "8px");

            H3 deliveryTitle = new H3("🚚 Información de Entrega");
            deliveryTitle.getStyle()
                    .set("margin-top", "0")
                    .set("color", "var(--lumo-success-color)");

            // Dirección de entrega
            if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().trim().isEmpty()) {
                HorizontalLayout addressLayout = new HorizontalLayout();
                addressLayout.setAlignItems(FlexComponent.Alignment.START);
                addressLayout.setSpacing(true);
                addressLayout.setWidthFull();

                Icon locationIcon = new Icon(VaadinIcon.MAP_MARKER);
                locationIcon.setColor("var(--lumo-error-color)");
                locationIcon.getStyle()
                        .set("font-size", "1.5em")
                        .set("margin-top", "5px");

                VerticalLayout addressText = new VerticalLayout();
                addressText.setPadding(false);
                addressText.setSpacing(false);

                Span addressLabel = new Span("Dirección de entrega:");
                addressLabel.getStyle()
                        .set("font-weight", "bold")
                        .set("margin-bottom", "5px");

                Span addressValue = new Span(order.getDeliveryAddress());
                addressValue.getStyle()
                        .set("font-size", "1.1em")
                        .set("padding", "10px")
                        .set("background", "white")
                        .set("border-radius", "6px")
                        .set("border", "1px solid var(--lumo-contrast-20pct)");

                addressText.add(addressLabel, addressValue);
                addressLayout.add(locationIcon, addressText);
                deliveryInfo.add(addressLayout);
            }

            // Repartidor asignado
            if (order.getDeliveryTo() != null && !order.getDeliveryTo().trim().isEmpty()) {
                HorizontalLayout courierLayout = new HorizontalLayout();
                courierLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                courierLayout.setSpacing(true);

                Icon courierIcon = new Icon(VaadinIcon.USER);
                courierIcon.setColor("var(--lumo-primary-color)");
                courierIcon.getStyle().set("font-size", "1.2em");

                Span courierLabel = new Span("Repartidor asignado:");
                courierLabel.getStyle().set("font-weight", "bold");

                Span courierValue = new Span(order.getDeliveryTo());
                courierValue.getStyle()
                        .set("font-size", "1.1em")
                        .set("font-weight", "500");

                courierLayout.add(courierIcon, courierLabel, courierValue);
                deliveryInfo.add(courierLayout);
            }

            // Lista de productos
            VerticalLayout productsLayout = new VerticalLayout();
            productsLayout.setPadding(false);
            productsLayout.setSpacing(true);
            productsLayout.getStyle()
                    .set("margin-top", "20px")
                    .set("padding", "15px")
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "8px");

            H3 productsTitle = new H3("🛍️ Productos del Pedido");
            productsTitle.getStyle()
                    .set("margin-top", "0")
                    .set("color", "var(--lumo-primary-color)");
            productsLayout.add(productsTitle);

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                double total = 0.0;

                // Encabezado de la tabla
                HorizontalLayout headerLayout = new HorizontalLayout();
                headerLayout.setWidthFull();
                headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
                headerLayout.getStyle()
                        .set("font-weight", "bold")
                        .set("border-bottom", "2px solid var(--lumo-contrast-20pct)")
                        .set("padding-bottom", "10px")
                        .set("margin-bottom", "10px");

                Span headerProduct = new Span("Producto");
                headerProduct.setWidth("45%");

                Span headerQty = new Span("Cant.");
                headerQty.setWidth("15%");
                headerQty.getStyle().set("text-align", "center");

                Span headerPrice = new Span("Precio");
                headerPrice.setWidth("20%");
                headerPrice.getStyle().set("text-align", "right");

                Span headerSubtotal = new Span("Subtotal");
                headerSubtotal.setWidth("20%");
                headerSubtotal.getStyle().set("text-align", "right");

                headerLayout.add(headerProduct, headerQty, headerPrice, headerSubtotal);
                productsLayout.add(headerLayout);

                // Items del pedido
                for (OrderItem item : order.getItems()) {
                    HorizontalLayout itemLayout = new HorizontalLayout();
                    itemLayout.setWidthFull();
                    itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    itemLayout.getStyle()
                            .set("padding", "10px 0")
                            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

                    String productName = "Producto";
                    if (item.getProduct() != null && item.getProduct().getName() != null) {
                        productName = item.getProduct().getName();
                    }

                    Span nameSpan = new Span(productName);
                    nameSpan.setWidth("45%");
                    nameSpan.getStyle()
                            .set("font-weight", "500")
                            .set("padding-left", "5px");

                    Span qtySpan = new Span("x" + item.getQuantity());
                    qtySpan.setWidth("15%");
                    qtySpan.getStyle()
                            .set("text-align", "center")
                            .set("background", "var(--lumo-primary-10pct)")
                            .set("padding", "5px 10px")
                            .set("border-radius", "15px")
                            .set("font-weight", "bold");

                    Span priceSpan = new Span(String.format("$%.2f", item.getUnitPrice()));
                    priceSpan.setWidth("20%");
                    priceSpan.getStyle()
                            .set("text-align", "right")
                            .set("color", "var(--lumo-secondary-text-color)");

                    double subtotal = item.getUnitPrice() * item.getQuantity();
                    total += subtotal;

                    Span subtotalSpan = new Span(String.format("$%.2f", subtotal));
                    subtotalSpan.setWidth("20%");
                    subtotalSpan.getStyle()
                            .set("font-weight", "bold")
                            .set("text-align", "right");

                    itemLayout.add(nameSpan, qtySpan, priceSpan, subtotalSpan);
                    productsLayout.add(itemLayout);
                }

                // Total del pedido
                HorizontalLayout totalLayout = new HorizontalLayout();
                totalLayout.setWidthFull();
                totalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
                totalLayout.getStyle()
                        .set("margin-top", "20px")
                        .set("padding-top", "15px")
                        .set("border-top", "2px solid var(--lumo-contrast-30pct)")
                        .set("font-size", "1.1em");

                Span totalLabel = new Span("TOTAL DEL PEDIDO:");
                totalLabel.getStyle()
                        .set("font-weight", "bold")
                        .set("margin-right", "20px");

                Span totalValue = new Span(String.format("$%.2f", total));
                totalValue.getStyle()
                        .set("font-weight", "bold")
                        .set("color", "var(--lumo-primary-color)")
                        .set("font-size", "1.3em");

                totalLayout.add(totalLabel, totalValue);
                productsLayout.add(totalLayout);
            } else {
                // Si no hay items
                Span noItems = new Span("⚠️ No hay productos registrados en este pedido");
                noItems.getStyle()
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("font-style", "italic")
                        .set("text-align", "center")
                        .set("padding", "30px")
                        .set("font-size", "1.1em");
                productsLayout.add(noItems);
            }

            // Botones del diálogo
            HorizontalLayout buttons = new HorizontalLayout();
            buttons.setWidthFull();
            buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            buttons.setSpacing(true);

            Button closeBtn = new Button("Cerrar", e -> dialog.close());
            closeBtn.setThemeName("tertiary");

            Button markDeliveredBtn = new Button("✅ Entregar Pedido", e -> {
                markAsDelivered(order);
                dialog.close();
            });
            markDeliveredBtn.setThemeName("primary success");
            markDeliveredBtn.getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "1.1em");

            buttons.add(closeBtn, markDeliveredBtn);

            // Layout principal del diálogo
            VerticalLayout dialogLayout = new VerticalLayout();
            dialogLayout.setSpacing(true);
            dialogLayout.setPadding(true);
            dialogLayout.setWidthFull();

            dialogLayout.add(dialogTitle, orderInfo, deliveryTitle, deliveryInfo, productsLayout, buttons);

            dialog.add(dialogLayout);
            dialog.open();

        } catch (Exception e) {
            log.error("Error al mostrar detalles del pedido #{}", orderId, e);
            Notification.show("Error: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
        }
    }

    private void markAsDelivered(Order order) {
        try {
            String me = getCurrentUsername();
            if (me == null) {
                throw new IllegalStateException("No se pudo identificar al repartidor");
            }

            orderService.markDelivered(order.getId(), me);

            // 🔥 Recargar el pedido COMPLETO desde la BD
            Order updated = orderService.findDeliveryOrderWithDetails(order.getId(), me);

            // 🔥 Ahora sí enviar el email
            emailService.enviarConfirmacionEntrega(updated);


            Notification.show("✅ Pedido #" + order.getId() + " marcado como ENTREGADO",
                    3000, Notification.Position.MIDDLE);

            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.access(() -> {
                    try {
                        Thread.sleep(300);
                        refresh();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

        } catch (Exception ex) {
            log.error("Error al marcar pedido como entregado", ex);
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