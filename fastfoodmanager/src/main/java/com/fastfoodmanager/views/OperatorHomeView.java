package com.fastfoodmanager.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Operario | FastTasty")
@Route(value = "operator/home", layout = MainLayout.class)
@RolesAllowed("OPERATOR")
public class OperatorHomeView extends VerticalLayout {

    public OperatorHomeView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H1("Panel de Operario"),
                new Paragraph("Gestiona pedidos y stock desde el menú superior.")
        );
    }
}
