package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@PageTitle("Login")
@Route("login")
public class LoginView extends VerticalLayout {

    private final UserService userService;

    public LoginView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("🍔 FastTasty");
        LoginForm login = new LoginForm();
        login.setForgotPasswordButtonVisible(false);

        // Enlace a registro
        Anchor signup = new Anchor("register", "¿No tienes cuenta? Crear cuenta");
        HorizontalLayout under = new HorizontalLayout(signup);
        under.setPadding(false);
        under.setSpacing(false);

        login.addLoginListener(e -> doLogin(e.getUsername(), e.getPassword()));

        add(title, login, under);
    }

    private void doLogin(String username, String rawPassword) {
        if (userService.authenticate(username, rawPassword)) {
            User user = userService.findByUsername(username).orElseThrow();
            VaadinSession.getCurrent().setAttribute(User.class, user);

            if (user.getRole() == Role.ADMIN) {
                UI.getCurrent().navigate("admin/users");
            } else {
                UI.getCurrent().navigate("home");
            }
        } else {
            Notification.show("Credenciales inválidas", 2500, Notification.Position.MIDDLE);
        }
    }
}
