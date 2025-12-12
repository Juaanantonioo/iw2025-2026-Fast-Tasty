package com.fastfoodmanager.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Repartidor | FastTasty")
@Route(value = "delivery/home", layout = MainLayout.class)
@RolesAllowed("DELIVERY")
public class DeliveryHomeView extends VerticalLayout {

    public DeliveryHomeView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(
                new H1("Panel de Reparto"),
                new Paragraph("Aquí verás los pedidos LISTO para entregar.")
        );
    }
}
