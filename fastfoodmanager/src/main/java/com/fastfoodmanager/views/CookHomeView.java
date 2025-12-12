package com.fastfoodmanager.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Cocinero | FastTasty")
@Route(value = "cook/home", layout = MainLayout.class)
@RolesAllowed("COOK")
public class CookHomeView extends VerticalLayout {

    public CookHomeView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H1("Panel de Cocina"),
                new Paragraph("Revisa pedidos en cocina y marca HECHO cuando termines.")
        );
    }
}
