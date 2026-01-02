package com.fastfoodmanager.views;

import com.vaadin.flow.component.html.Div;
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
        setPadding(false);
        setSpacing(false);

        addClassName("dashboard-bg");

        Div page = new Div();
        page.addClassName("ft-page");

        Div header = new Div();
        header.addClassName("ft-topbar");

        Div headerBlock = new Div();
        H1 title = new H1("Panel de Operario");
        title.addClassName("ft-title");

        Paragraph subtitle = new Paragraph("Gestiona pedidos, stock, caja y estadísticas desde el menú superior.");
        subtitle.addClassName("ft-subtitle");

        headerBlock.add(title, subtitle);

        header.add(headerBlock);

        Div card = new Div();
        card.addClassName("ft-card");
        card.add(new Paragraph("Accede a tus secciones desde la barra de navegación para trabajar con pedidos y operaciones del día."));

        page.add(header, card);

        add(page);
    }
}