package com.fastfoodmanager.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("FastTasty | Bienvenido")
@Route(value = "")
@CssImport("./themes/my-theme/welcome.css")
public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        addClassName("welcome-view");
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Hero principal
        Div hero = new Div();
        hero.addClassName("welcome-hero");

        H1 title = new H1("Bienvenido a FastTasty 🍔");
        Paragraph desc = new Paragraph("Comida deliciosa, rápida y al alcance de un clic.");
        Button start = new Button("Ver Carta", e -> UI.getCurrent().navigate("carta"));
        start.addClassName("welcome-button");

        hero.add(title, desc, start);
        add(hero);
    }
}
