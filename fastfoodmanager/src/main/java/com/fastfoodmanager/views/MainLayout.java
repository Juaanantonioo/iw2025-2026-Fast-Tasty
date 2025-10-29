package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout() {
        // 🔹 Título
        H1 title = new H1("🍔 FastTasty");
        title.getStyle()
                .set("font-size", "1.4rem")
                .set("margin", "0")
                .set("color", "#ff5c1a");

        // 🔹 Enlaces comunes
        RouterLink home = new RouterLink("Inicio", WelcomeView.class);
        RouterLink carta = new RouterLink("Carta", CartaView.class);

        HorizontalLayout tabs = new HorizontalLayout(home, carta);
        tabs.setSpacing(true);
        tabs.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // 🔹 Usuario actual
        User current = VaadinSession.getCurrent().getAttribute(User.class);

        // 🔹 Si es ADMIN, añadimos enlace "Productos"
        if (current != null && current.getRole() == Role.ADMIN) {
            RouterLink products = new RouterLink("Productos", ProductView.class);
            tabs.add(products);
        }

        // 🔹 Sección derecha (logout)
        HorizontalLayout right = new HorizontalLayout();
        right.setSpacing(true);
        right.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        if (current != null) {
            Button logout = new Button("Salir", e -> {
                VaadinSession.getCurrent().getSession().invalidate();
                VaadinSession.getCurrent().close();
                UI.getCurrent().navigate("login");
            });
            logout.getStyle()
                    .set("background-color", "#f7f7f7")
                    .set("color", "#333")
                    .set("border-radius", "8px")
                    .set("padding", "4px 12px")
                    .set("cursor", "pointer");
            right.add(logout);
        }

        // 🔹 Barra superior
        HorizontalLayout bar = new HorizontalLayout(title, tabs, right);
        bar.addClassName("app-topbar");
        bar.setWidthFull();
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        bar.expand(tabs);
        bar.setSpacing(true);
        bar.getStyle()
                .set("padding", "0.6rem 1.2rem")
                .set("background", "white")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

        addToNavbar(bar);
    }
}
