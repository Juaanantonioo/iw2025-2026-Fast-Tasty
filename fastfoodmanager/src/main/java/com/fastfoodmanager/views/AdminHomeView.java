package com.fastfoodmanager.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Admin | FastTasty")
@Route(value = "admin/home", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminHomeView extends VerticalLayout {

    public AdminHomeView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H1("Panel de Administración"),
                new Paragraph("Desde aquí puedes gestionar productos y crear/gestionar encargados (MANAGER).")
        );
    }
}
