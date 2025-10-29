package com.fastfoodmanager.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

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
        setJustifyContentMode(JustifyContentMode.CENTER);

        // ====== CARRUSEL DE FONDO ======
        Div bgCarousel = new Div();
        bgCarousel.addClassName("bg-carousel");

        List<String> images = List.of(
                "/images/BBQ-BURGER.png",
                "/images/burguer_clasica.jpg",
                "/images/cheeseburger.jpg",
                "/images/classic_smash.jpg",
                "/images/doble_cheeseburger.jpg",
                "/images/lagenerosa.webp",
                "https://media.traveler.es/photos/67d05d6cf405fe40a66bfa66/master/w_1600,c_limit/foodberzo.jpg"
        );

        for (int i = 0; i < images.size(); i++) {
            Div slide = new Div();
            slide.addClassName("bg-slide");
            slide.getStyle().set("background-image", "url('" + images.get(i) + "')");

            // 👇 Fijamos el delay directamente (7s por slide)
            slide.getStyle().set("animation-delay", (i * 7) + "s");

            bgCarousel.add(slide);
        }
        add(bgCarousel);

        // ====== HERO (CTA) ======
        Div hero = new Div();
        hero.addClassName("welcome-hero");
        hero.addClassName("content-layer");   // <<---
        H1 title = new H1("Bienvenido a FastTasty 🍔");
        Paragraph desc = new Paragraph("Comida deliciosa, rápida y al alcance de un clic.");
        Button start = new Button("Ver Carta", e -> UI.getCurrent().navigate("carta"));
        start.addClassName("welcome-button");
        hero.add(title, desc, start);
        add(hero);

        // ====== UBICACIÓN & HORARIOS ======
        Div infoBlock = new Div();
        infoBlock.addClassName("info-block");
        infoBlock.addClassName("content-layer"); // <<---

        Div locationCol = new Div();
        locationCol.addClassName("info-col");
        H2 locTitle = new H2("📍 Nuestra ubicación");
        Paragraph address = new Paragraph("Calle Ejemplo 123, Sevilla");
        IFrame map = new IFrame("https://www.google.com/maps?q=Calle+Ejemplo+123+Sevilla&output=embed");
        map.addClassName("map-embed");
        map.setTitle("Mapa del restaurante");
        map.getElement().setAttribute("loading", "lazy");
        map.getElement().setAttribute("referrerpolicy", "no-referrer-when-downgrade");
        locationCol.add(locTitle, address, map);

        Div hoursCol = new Div();
        hoursCol.addClassName("info-col");
        H2 hoursTitle = new H2("🕒 Horarios");
        UnorderedList hours = new UnorderedList();
        hours.add(new ListItem("Lunes - Jueves: 12:30 – 16:00 / 20:00 – 23:30"));
        hours.add(new ListItem("Viernes: 12:30 – 16:00 / 20:00 – 00:00"));
        hours.add(new ListItem("Sábado: 13:00 – 00:00"));
        hours.add(new ListItem("Domingo: 13:00 – 23:30"));
        hoursCol.add(hoursTitle, hours);

        HorizontalLayout infoRow = new HorizontalLayout(locationCol, hoursCol);
        infoRow.addClassName("info-row");
        infoRow.setWidthFull();
        infoRow.setPadding(true);
        infoRow.setSpacing(true);
        infoRow.setDefaultVerticalComponentAlignment(Alignment.START);

        infoBlock.add(infoRow);
        add(infoBlock);
    }
}