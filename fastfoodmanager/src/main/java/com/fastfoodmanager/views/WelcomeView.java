package com.fastfoodmanager.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("FastTasty | Bienvenido")
@Route("")
@CssImport("./themes/my-theme/welcome.css")
@AnonymousAllowed
public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        addClassName("welcome-view");
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        // ===== Fondo carrusel =====
        Div bgCarousel = new Div();
        bgCarousel.addClassName("bg-carousel");

        List<String> images = List.of(
                "/images/BBQ-BURGER.png",
                "/images/BOMBA_SEXY.png",
                "/images/clasica.jpg",
                "/images/doble_cheese.jpeg",
                "/images/ensalada_cesar.jpg",
                "/images/ensalada_mixta.jpeg",
                "/images/La_iberica.jpg",
                "/images/La_spanish.avif",
                "/images/patatas_con_queso_y_bacon.jpg",
                "/images/trufada_rustica.avif",
                "https://media.traveler.es/photos/67d05d6cf405fe40a66bfa66/master/w_1600,c_limit/foodberzo.jpg"
        );

        for (int i = 0; i < images.size(); i++) {
            Div slide = new Div();
            slide.addClassName("bg-slide");
            slide.getStyle().set("background-image", "url('" + images.get(i) + "')");
            slide.getStyle().set("animation-delay", (i * 7) + "s");
            bgCarousel.add(slide);
        }
        add(bgCarousel);

        // ===== TARJETA HERO con pestañas dentro =====
        Div hero = new Div();
        hero.addClassName("welcome-hero");
        hero.addClassName("content-layer");

        // Encabezado del hero
        Div heroHeader = new Div();
        heroHeader.addClassName("hero-header");

        H1 title = new H1("Bienvenido a FastTasty 🍔");
        Paragraph desc = new Paragraph("Comida deliciosa, rápida y al alcance de un clic.");
        Button start = new Button("Ver Carta", e -> UI.getCurrent().navigate("carta"));
        start.addClassName("welcome-button");
        heroHeader.add(title, desc, start);

        // Barra de pestañas
        Tab tabUbicacion = new Tab("Ubicación");
        Tab tabHorarios = new Tab("Horarios");
        Tab tabReservas = new Tab("Reservas");
        Tabs tabs = new Tabs(tabUbicacion, tabHorarios, tabReservas);
        tabs.setAutoselect(true);
        tabs.setWidthFull();
        tabs.getElement().setAttribute("theme", "centered small");
        tabs.addClassName("hero-tabs");

        // Paneles (contenidos) dentro del hero
        Div panelContainer = new Div();
        panelContainer.addClassName("hero-panels");

        // Ubicación
        Div panelUbicacion = new Div();
        panelUbicacion.addClassName("hero-panel");
        H2 locTitle = new H2("📍 Nuestra ubicación");
        Paragraph address = new Paragraph("Calle Ejemplo 123, Sevilla");
        IFrame map = new IFrame("https://www.google.com/maps?q=Calle+Ejemplo+123+Sevilla&output=embed");
        map.addClassName("map-embed");
        map.setTitle("Mapa del restaurante");
        map.getElement().setAttribute("loading", "lazy");
        map.getElement().setAttribute("referrerpolicy", "no-referrer-when-downgrade");
        panelUbicacion.add(locTitle, address, map);

        // Horarios
        Div panelHorarios = new Div();
        panelHorarios.addClassName("hero-panel");
        H2 hoursTitle = new H2("🕒 Horarios");
        UnorderedList hours = new UnorderedList();
        hours.addClassName("horarios");
        hours.add(new ListItem("Lunes - Jueves: 12:30 – 16:00 / 20:00 – 23:30"));
        hours.add(new ListItem("Viernes: 12:30 – 16:00 / 20:00 – 00:00"));
        hours.add(new ListItem("Sábado: 13:00 – 00:00"));
        hours.add(new ListItem("Domingo: 13:00 – 23:30"));
        panelHorarios.add(hoursTitle, hours);

        // Reservas
        Div panelReservas = new Div();
        panelReservas.addClassName("hero-panel");
        H2 resTitle = new H2("Reserva tu mesa");

        FormLayout form = new FormLayout();
        form.addClassName("booking-form");

        TextField nombre = new TextField("Nombre");
        nombre.setRequired(true);
        TextField telefono = new TextField("Teléfono");
        telefono.setRequired(true);
        EmailField email = new EmailField("Email");
        email.setRequiredIndicatorVisible(true);

        DatePicker fecha = new DatePicker("Fecha");
        fecha.setMin(LocalDate.now());
        fecha.setRequired(true);

        TimePicker hora = new TimePicker("Hora");
        hora.setStep(java.time.Duration.ofMinutes(15));
        hora.setMin(LocalTime.of(12, 0));
        hora.setMax(LocalTime.of(23, 30));
        hora.setRequired(true);

        IntegerField personas = new IntegerField("Comensales");
        personas.setStepButtonsVisible(true);
        personas.setMin(1);
        personas.setMax(12);
        personas.setValue(2);

        TextArea comentarios = new TextArea("Comentarios (opcional)");
        comentarios.setMaxLength(300);
        comentarios.setPlaceholder("Alergias, trona, preferencia de mesa…");

        form.add(nombre, telefono, email, fecha, hora, personas);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("640px", 2),
                new FormLayout.ResponsiveStep("1024px", 3)
        );

        Button reservar = new Button("Confirmar reserva", e -> {
            if (nombre.isEmpty() || telefono.isEmpty() || email.isEmpty()
                    || fecha.isEmpty() || hora.isEmpty() || personas.isEmpty()) {
                Notification.show("Rellena todos los campos obligatorios.", 3000, Notification.Position.TOP_CENTER);
                return;
            }
            Notification.show("Reserva enviada. Te confirmaremos por email o WhatsApp.", 3500, Notification.Position.TOP_CENTER);
        });
        reservar.addClassName("primary-btn");

        panelReservas.add(resTitle, form, comentarios, reservar);

        panelContainer.add(panelUbicacion, panelHorarios, panelReservas);

        // Añadimos todo dentro del hero
        hero.add(heroHeader, tabs, panelContainer);
        add(hero);

        // ===== Conmutación de paneles dentro del hero =====
        Map<Tab, Div> panelByTab = new HashMap<>();
        panelByTab.put(tabUbicacion, panelUbicacion);
        panelByTab.put(tabHorarios, panelHorarios);
        panelByTab.put(tabReservas, panelReservas);

        // Estado inicial
        panelByTab.values().forEach(p -> p.setVisible(false));
        panelByTab.get(tabUbicacion).setVisible(true);
        tabs.setSelectedTab(tabUbicacion);

        tabs.addSelectedChangeListener(e -> {
            panelByTab.values().forEach(p -> p.setVisible(false));
            Div panel = panelByTab.get(e.getSelectedTab());
            if (panel != null) panel.setVisible(true);
        });
    }
}
