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

        H1 title = new H1("🍔 FastTasty");
        title.getStyle()
                .set("font-size", "1.4rem")
                .set("margin", "0")
                .set("color", "#ff5c1a")
                .set("white-space", "nowrap");

        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setSpacing(true);
        tabs.setPadding(false);
        tabs.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        tabs.getStyle().set("flex-wrap", "wrap"); // evita overflow si hay muchas pestañas

        if (!isAuthenticated()) {
            tabs.add(new RouterLink("Carta", CartaView.class));
            tabs.add(new RouterLink("Entrar", LoginView.class));
        }

        if (hasRole("USER")) {
            tabs.add(new RouterLink("Carta", CartaView.class));
            tabs.add(new RouterLink("Pedido", CarritoView.class));
            tabs.add(new RouterLink("Mis pedidos", ClientOrdersView.class));
            tabs.add(new RouterLink("Perfil", ProfileView.class));
        }

        if (hasRole("OPERATOR")) {
            tabs.add(new RouterLink("Inicio", OperatorHomeView.class));
            tabs.add(new RouterLink("Pedidos", OperatorOrdersView.class));
            tabs.add(new RouterLink("Stock", OperatorStockView.class));
            tabs.add(new RouterLink("Caja", OperatorCashView.class));
            tabs.add(new RouterLink("Estadísticas", OperatorStatsView.class));
        }

        if (hasRole("COOK")) {
            tabs.add(new RouterLink("Inicio", CookHomeView.class));
            tabs.add(new RouterLink("Cocina", CookOrdersView.class));
        }

        if (hasRole("DELIVERY")) {
            tabs.add(new RouterLink("Inicio", DeliveryHomeView.class));
            tabs.add(new RouterLink("Repartos", DeliveryOrdersView.class));
        }

        if (hasRole("MANAGER")) {
            tabs.add(new RouterLink("Inicio", ManagerHomeView.class));
            tabs.add(new RouterLink("Gestión de usuarios", ManagerUsersView.class));
        }

        if (hasRole("ADMIN")) {
            tabs.add(new RouterLink("Inicio", AdminHomeView.class));
            tabs.add(new RouterLink("Gestionar productos", ProductView.class));
            tabs.add(new RouterLink("Gestionar encargados", AdminManagersView.class));
            tabs.add(new RouterLink("Gestionar categorías", AdminFoodTypeView.class));
        }

        HorizontalLayout right = new HorizontalLayout();
        right.setSpacing(true);
        right.setPadding(false);
        right.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        right.getStyle().set("white-space", "nowrap");

        if (isAuthenticated()) {
            right.add(new Span("Hola, " + getUsername()));

            Button logout = new Button("Salir", e -> {
                VaadinSession.getCurrent().getSession().invalidate();
                VaadinSession.getCurrent().close();
                UI.getCurrent().navigate("login");
            });

            logout.getStyle()
                    .set("background-color", "#f7f7f7")
                    .set("color", "#333")
                    .set("border-radius", "999px")
                    .set("padding", "6px 14px")
                    .set("font-weight", "700")
                    .set("cursor", "pointer");

            right.add(logout);
        }

        HorizontalLayout bar = new HorizontalLayout(title, tabs, right);
        bar.setWidthFull();
        bar.setSpacing(true);
        bar.setPadding(true);
        bar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        bar.expand(tabs);

        bar.addClassName("app-topbar");
        bar.getStyle()
                .set("padding", "0.65rem 1.2rem")
                .set("background", "rgba(255,255,255,0.85)")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.06)")
                .set("border-bottom", "1px solid rgba(0,0,0,0.06)")
                .set("overflow", "hidden"); // último seguro anti-overflow

        addToNavbar(bar);
    }

    private boolean isAuthenticated() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.isAuthenticated()
                && !"anonymousUser".equals(String.valueOf(a.getPrincipal()));
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