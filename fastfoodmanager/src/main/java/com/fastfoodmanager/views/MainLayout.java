package com.fastfoodmanager.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout() {
        // Logo / título
        H1 title = new H1("🍔 FastTasty");
        title.getStyle()
                .set("font-size", "1.4rem")
                .set("margin", "0")
                .set("color", "#ff5c1a");

        // Enlaces comunes
        RouterLink home  = new RouterLink("Inicio", WelcomeView.class);
        RouterLink carta = new RouterLink("Carta",  CartaView.class);

        HorizontalLayout tabs = new HorizontalLayout(home, carta);
        tabs.setSpacing(true);
        tabs.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        // 👉 Enlaces ADMIN
        if (hasRole("ADMIN")) {
            RouterLink productos = new RouterLink("Gestionar productos", ProductView.class);
            RouterLink usuarios  = new RouterLink("Gestionar usuarios", AdminUsersView.class);
            tabs.add(productos, usuarios);
        }

        // 👉 Enlaces OPERADOR
        if (hasRole("OPERATOR")) {
            tabs.add(new RouterLink("Pedidos", OperatorOrdersView.class));
            tabs.add(new RouterLink("Stock", OperatorStockView.class));
        }

        // Lado derecho: saludo + Entrar/Salir
        HorizontalLayout right = new HorizontalLayout();
        right.setSpacing(true);
        right.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        if (isAuthenticated()) {
            right.add(new Span("Hola, " + getUsername()));
            Button logout = new Button("Salir", e -> {
                VaadinSession.getCurrent().getSession().invalidate();
                VaadinSession.getCurrent().close();
                UI.getCurrent().navigate(""); // vuelve al inicio
            });
            logout.getStyle()
                    .set("background-color", "#f7f7f7")
                    .set("color", "#333")
                    .set("border-radius", "8px")
                    .set("padding", "6px 14px")
                    .set("font-weight", "600")
                    .set("cursor", "pointer");
            right.add(logout);
        } else {
            Button login = new Button("Entrar", e -> UI.getCurrent().navigate("login"));
            login.getStyle()
                    .set("background-color", "#ff7b00")
                    .set("color", "#fff")
                    .set("border-radius", "8px")
                    .set("padding", "6px 14px")
                    .set("font-weight", "600");
            right.add(login);
        }

        // Barra superior
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

    private boolean isAuthenticated() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.isAuthenticated() && !"anonymousUser".equals(String.valueOf(a.getPrincipal()));
    }

    private String getUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return (a != null) ? a.getName() : "Invitado";
    }

    private boolean hasRole(String role) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return false;
        String needed = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        for (GrantedAuthority ga : a.getAuthorities()) {
            if (needed.equals(ga.getAuthority())) return true;
        }
        return false;
    }
}
