package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.OrderType;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.CartService;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
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
            Button minus = new Button("−", e -> {
                cartService.decrement(i);
                refresh();
            });

            Button plus = new Button("+", e -> {
                cartService.add(i.getProduct());
                refresh();
            });

            Button del = new Button("Eliminar", e -> {
                cartService.remove(i);
                refresh();
            });

            return new HorizontalLayout(minus, plus, del);
        }).setHeader("Acciones").setAutoWidth(true);

        Button backToCarta = new Button("← Volver a la carta", e -> UI.getCurrent().navigate("carta"));

        Button pay = new Button("💳 Proceder al pago", e -> showOrderTypeDialog());
        pay.getStyle().set("background-color", "#0070ba").set("color", "white").set("font-weight", "600");

        totalLabel.getStyle().set("font-weight", "700");

        add(title, grid, totalLabel, new HorizontalLayout(backToCarta, pay));
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        List<OrderItem> items = cartService.getItems();
        grid.setItems(items);

        if (items.isEmpty()) {
            totalLabel.setText("Tu carrito está vacío");
        } else {
            totalLabel.setText("Total: " + String.format("€ %.2f", cartService.total()) +
                    " | Artículos: " + cartService.countItems());
        }

        grid.getDataProvider().refreshAll();
    }

    private void showOrderTypeDialog() {
        System.out.println("DEBUG: showOrderTypeDialog llamado");

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

        // Crear el diálogo directamente aquí
        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        H2 title = new H2("¿Cómo quieres recibir tu pedido?");
        title.getStyle().set("margin-top", "0").set("color", "#2c3e50");

        Paragraph info = new Paragraph("Selecciona una opción para continuar con el pago");
        info.getStyle().set("color", "#7f8c8d");

        // CHECKBOX PARA ENVÍO DE EMAIL
        Checkbox emailCheckbox = new Checkbox("📧 Enviar ticket al correo electrónico");
        emailCheckbox.setValue(true); // Marcado por defecto
        emailCheckbox.getStyle().set("margin", "15px 0");

        // CORRECCIÓN: No usar info.add() ni emailCheckbox.add()
        // Crear un layout para mostrar la información del email
        VerticalLayout emailInfoLayout = new VerticalLayout();
        emailInfoLayout.setSpacing(false);
        emailInfoLayout.setPadding(false);
        emailInfoLayout.setMargin(false);

        // Verificar si el usuario tiene email registrado
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            emailCheckbox.setEnabled(false);
            emailCheckbox.setLabel("📧 Enviar ticket al correo (no tienes email registrado)");

            Span warning = new Span("⚠️ Para recibir el ticket por email, actualiza tu perfil con tu dirección de correo.");
            warning.getStyle()
                    .set("color", "orange")
                    .set("font-size", "12px")
                    .set("margin-top", "5px");

            emailInfoLayout.add(emailCheckbox, warning);
        } else {
            Span emailInfo = new Span("Se enviará a: " + user.getEmail());
            emailInfo.getStyle()
                    .set("color", "#0070ba")
                    .set("font-size", "12px")
                    .set("margin-top", "5px");

            emailInfoLayout.add(emailCheckbox, emailInfo);
        }

        // Botón para recoger en local
        Button pickupBtn = new Button("🏪 Recoger en local", e -> {
            System.out.println("DEBUG: Pickup seleccionado");
            dialog.close();
            processPayment(user, OrderType.PICKUP, null, emailCheckbox.getValue());
        });
        pickupBtn.getStyle()
                .set("background-color", "#0070ba")
                .set("color", "white")
                .set("font-weight", "600")
                .set("padding", "15px 30px")
                .set("font-size", "16px")
                .set("margin", "10px")
                .set("cursor", "pointer");

        // Botón para domicilio
        Button deliveryBtn = new Button("🚚 A domicilio", e -> {
            System.out.println("DEBUG: Delivery seleccionado");
            dialog.close();
            showAddressDialog(user, emailCheckbox.getValue());
        });
        deliveryBtn.getStyle()
                .set("background-color", "#28a745")
                .set("color", "white")
                .set("font-weight", "600")
                .set("padding", "15px 30px")
                .set("font-size", "16px")
                .set("margin", "10px")
                .set("cursor", "pointer");

        // Layout para los botones
        HorizontalLayout buttonsLayout = new HorizontalLayout(pickupBtn, deliveryBtn);
        buttonsLayout.setSpacing(true);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        // Layout principal - CORREGIDO: usar emailInfoLayout en lugar de emailCheckbox solo
        VerticalLayout layout = new VerticalLayout(title, info, emailInfoLayout, buttonsLayout);
        layout.setAlignItems(Alignment.CENTER);
        layout.setSpacing(true);
        layout.setPadding(true);

        dialog.add(layout);
        System.out.println("DEBUG: Abriendo diálogo de tipo de pedido");
        dialog.open();
    }

    private void showAddressDialog(User user, boolean enviarEmail) {
        System.out.println("DEBUG: showAddressDialog llamado - Enviar email: " + enviarEmail);

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        H2 title = new H2("📦 Dirección de entrega");
        title.getStyle().set("color", "#2c3e50");

        Paragraph info = new Paragraph("Por favor, ingresa la dirección completa para la entrega a domicilio");
        info.getStyle().set("color", "#7f8c8d");

        TextField streetField = new TextField("Calle y número");
        streetField.setWidthFull();
        streetField.setRequired(true);
        streetField.setPlaceholder("Ej: Av. Principal 123");
        streetField.setErrorMessage("Este campo es obligatorio");

        TextField floorField = new TextField("Piso y departamento (opcional)");
        floorField.setWidthFull();
        floorField.setPlaceholder("Ej: Piso 3, Depto. B");

        TextField cityField = new TextField("Ciudad");
        cityField.setWidthFull();
        cityField.setRequired(true);
        cityField.setPlaceholder("Ej: Madrid");
        cityField.setErrorMessage("Este campo es obligatorio");

        TextField zipCodeField = new TextField("Código postal");
        zipCodeField.setWidthFull();
        zipCodeField.setRequired(true);
        zipCodeField.setPattern("[0-9]*");
        zipCodeField.setPlaceholder("Ej: 28001");
        zipCodeField.setErrorMessage("Código postal inválido");

        TextArea additionalInfo = new TextArea("Instrucciones adicionales (opcional)");
        additionalInfo.setWidthFull();
        additionalInfo.setPlaceholder("Ej: Timbre 2 veces, dejar en portería, etc.");
        additionalInfo.setMaxHeight("100px");

        // Botón confirmar
        Button confirmBtn = new Button("✅ Confirmar y proceder al pago", e -> {
            boolean isValid = true;

            if (streetField.isEmpty()) {
                streetField.setInvalid(true);
                isValid = false;
            }

            if (cityField.isEmpty()) {
                cityField.setInvalid(true);
                isValid = false;
            }

            if (zipCodeField.isEmpty() || !zipCodeField.getValue().matches("[0-9]+")) {
                zipCodeField.setInvalid(true);
                isValid = false;
            }

            if (!isValid) {
                Notification.show("Por favor, completa los campos obligatorios correctamente", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Construir dirección completa
            StringBuilder addressBuilder = new StringBuilder();
            addressBuilder.append(streetField.getValue());

            if (!floorField.isEmpty()) {
                addressBuilder.append(", ").append(floorField.getValue());
            }

            addressBuilder.append(", ").append(cityField.getValue());
            addressBuilder.append(" ").append(zipCodeField.getValue());

            if (!additionalInfo.isEmpty()) {
                addressBuilder.append(". Instrucciones: ").append(additionalInfo.getValue());
            }

            String address = addressBuilder.toString();
            System.out.println("DEBUG: Dirección completa: " + address);

            dialog.close();
            processPayment(user, OrderType.DELIVERY, address, enviarEmail);
        });
        confirmBtn.getStyle()
                .set("background-color", "#28a745")
                .set("color", "white")
                .set("font-weight", "600")
                .set("margin-top", "20px");

        // Botón cancelar
        Button cancelBtn = new Button("↩️ Cambiar a recoger en local", e -> {
            System.out.println("DEBUG: Cambiando a recoger en local");
            dialog.close();
            processPayment(user, OrderType.PICKUP, null, enviarEmail);
        });
        cancelBtn.getStyle()
                .set("background-color", "#6c757d")
                .set("color", "white")
                .set("margin-top", "10px");

        // Formulario
        FormLayout form = new FormLayout();
        form.add(streetField, floorField, cityField, zipCodeField, additionalInfo);
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Layout principal
        VerticalLayout layout = new VerticalLayout(title, info, form, confirmBtn, cancelBtn);
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidth("600px");

        dialog.add(layout);
        System.out.println("DEBUG: Abriendo diálogo de dirección");
        dialog.open();
    }

    private void processPayment(User user, OrderType orderType, String deliveryAddress, boolean enviarEmail) {
        System.out.println("DEBUG: processPayment llamado - Tipo: " + orderType +
                ", Dirección: " + deliveryAddress +
                ", Enviar email: " + enviarEmail);

        try {
            // Crear pedido con el tipo y dirección
            Order order = orderService.createOrder(user, cartService.getItemsCopy(), orderType, deliveryAddress, enviarEmail);
            orderService.markAsPaid(order.getId());

            cartService.clear();
            refresh();

            // Mostrar mensaje según el tipo de pedido
            String message = "✅ Pedido #" + order.getId() + " creado y pagado exitosamente. ";

            if (enviarEmail && user.getEmail() != null && !user.getEmail().isEmpty()) {
                message += "Se ha enviado el ticket a " + user.getEmail() + ". ";
            } else if (enviarEmail) {
                message += "No se pudo enviar el ticket por email (no hay email registrado). ";
            }

            if (orderType == OrderType.PICKUP) {
                message += "Podrás recogerlo en nuestro local en aproximadamente 20-30 minutos.";
            } else {
                message += "Será entregado en tu domicilio en aproximadamente 40-50 minutos.";
                if (deliveryAddress != null) {
                    message += " Dirección: " + deliveryAddress;
                }
            }

            Notification.show(message, 7000, Notification.Position.MIDDLE);

            UI.getCurrent().navigate("client/orders");

        } catch (Exception e) {
            System.out.println("ERROR en processPayment: " + e.getMessage());
            e.printStackTrace();
            Notification.show("❌ Error al procesar el pedido: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}