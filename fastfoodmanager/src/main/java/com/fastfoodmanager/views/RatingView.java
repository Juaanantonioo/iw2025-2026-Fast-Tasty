package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("rating")
public class RatingView extends VerticalLayout {

    public RatingView(OrderService orderService, UserService userService) {

        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);

        // Cargar librería de confeti desde CDN
        UI.getCurrent().getPage().addJavaScript("https://cdn.jsdelivr.net/npm/canvas-confetti@1.6.0/dist/confetti.browser.min.js");

        // Obtener ID del pedido
        String orderIdParam = UI.getCurrent().getInternals().getActiveViewLocation()
                .getQueryParameters().getParameters().getOrDefault("order", List.of("0")).get(0);

        Long orderId = Long.valueOf(orderIdParam);
        String username = userService.getCurrentUsername();

        // Cargar pedido
        Order order = orderService.findById(orderId).orElse(null);

        if (order == null) {
            add(new H1("Pedido no encontrado"));
            return;
        }

        // 🔥 Si ya está valorado → mostrar mensaje y salir
        if (order.getRating() != null) {
            add(new H1("⭐ ¡Gracias por tu valoración!"));
            add(new Span("Ya valoraste este pedido con " + order.getRating() + " estrellas."));
            return;
        }

        add(new H1("⭐ Valora tu experiencia"));
        add(new Span("Selecciona cuántas estrellas nos das"));

        HorizontalLayout starsLayout = new HorizontalLayout();
        starsLayout.setSpacing(true);

        // Crear botones de estrellas
        for (int i = 1; i <= 5; i++) {
            int ratingValue = i;

            Button starBtn = new Button("⭐".repeat(i));

            // 🎨 Estilo base
            starBtn.getStyle()
                    .set("font-size", "26px")
                    .set("padding", "12px 18px")
                    .set("cursor", "pointer")
                    .set("background-color", "#ffe066")
                    .set("border-radius", "10px")
                    .set("border", "2px solid #e0b200")
                    .set("transition", "transform 0.2s ease, background-color 0.2s ease");

            // ✨ Animación al pasar el ratón
            starBtn.getElement().addEventListener("mouseover", e ->
                    starBtn.getStyle().set("transform", "scale(1.15)")
            );
            starBtn.getElement().addEventListener("mouseout", e ->
                    starBtn.getStyle().set("transform", "scale(1)")
            );

            // ⭐ Acción al votar
            starBtn.addClickListener(e -> {
                try {
                    orderService.saveRating(orderId, ratingValue, username);

                    // 🎉 Animación al votar
                    starBtn.getStyle()
                            .set("background-color", "#ffd43b")
                            .set("transform", "scale(1.3)");

                    // 🎊 CONFETI
                    UI.getCurrent().getPage().executeJs(
                            "confetti({" +
                                    "particleCount: 200," +
                                    "spread: 90," +
                                    "origin: { y: 0.6 }" +
                                    "});"
                    );

                    Notification.show("¡Gracias por tu valoración!", 3000, Notification.Position.MIDDLE);

                    // Reemplazar contenido
                    removeAll();
                    add(new H2("¡Gracias por tu valoración!"));
                    add(new Span("Has valorado este pedido con " + ratingValue + " estrellas ⭐"));

                } catch (Exception ex) {
                    removeAll();
                    add(new H2("No se pudo registrar tu valoración"));
                    add(new Span(ex.getMessage()));
                }
            });

            starsLayout.add(starBtn);
        }

        add(starsLayout);
    }
}
