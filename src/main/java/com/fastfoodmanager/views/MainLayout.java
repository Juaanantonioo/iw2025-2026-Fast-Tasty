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

public class MainLayout extends AppLayout {

    public MainLayout() {
        // Título
        H1 title = new H1("🍔 FastTasty");
        title.getStyle().set("font-size", "1.2rem").set("margin", "0");

        // Links comunes
        RouterLink home = new RouterLink("Inicio", HomeView.class);
        RouterLink products = new RouterLink("Productos", ProductView.class);

        HorizontalLayout tabs = new HorizontalLayout(home, products);
        tabs.setSpacing(true);

        // Parte derecha: Admin (si procede) + Logout
        HorizontalLayout right = new HorizontalLayout();
        right.setSpacing(true);

        User current = VaadinSession.getCurrent().getAttribute(User.class);
        if (current != null && current.getRole() == Role.ADMIN) {
            RouterLink admin = new RouterLink("Administración", AdminUsersView.class);
            right.add(admin);
        }

        Button logout = new Button("Salir", e -> {
            VaadinSession.getCurrent().getSession().invalidate();
            VaadinSession.getCurrent().close();
            UI.getCurrent().navigate("login");
        });
        right.add(logout);

        // Barra superior
        HorizontalLayout bar = new HorizontalLayout(title, tabs, right);
        bar.setWidthFull();
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        bar.expand(tabs);
        bar.setSpacing(true);
        bar.getStyle().set("padding", "0.5rem 1rem");

        addToNavbar(bar);
    }
}
