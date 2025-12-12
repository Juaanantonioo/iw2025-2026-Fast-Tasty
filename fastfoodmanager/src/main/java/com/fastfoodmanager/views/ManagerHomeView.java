package com.fastfoodmanager.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Encargado | FastTasty")
@Route(value = "manager/home", layout = MainLayout.class)
@RolesAllowed("MANAGER")
public class ManagerHomeView extends VerticalLayout {

    public ManagerHomeView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H1("Bienvenido, Encargado"),
                new Paragraph("Aquí puedes crear Operarios, Cocineros y Repartidores.")
        );
    }
}
