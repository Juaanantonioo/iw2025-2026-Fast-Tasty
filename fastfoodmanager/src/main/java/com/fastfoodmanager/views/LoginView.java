package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("login")
@PageTitle("Iniciar sesión | FastTasty")
public class LoginView extends VerticalLayout {

    public LoginView(UserService userService) {
        // Layout base
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // ----- Título y formulario (mismas variables que usamos después) -----
        H1 title = new H1("Fast&Tasty");
        LoginForm login = new LoginForm();
        login.setI18n(spanishI18n());
        login.setForgotPasswordButtonVisible(false);

        // Listener de login
        login.addLoginListener(e -> {
            final String username = e.getUsername();
            final String rawPassword = e.getPassword();

            if (userService.authenticate(username, rawPassword)) {
                User user = userService.findByUsername(username).orElseThrow();
                VaadinSession.getCurrent().setAttribute(User.class, user);

                if (user.getRole() == Role.ADMIN) {
                    UI.getCurrent().navigate("admin/users");
                } else {
                    UI.getCurrent().navigate(""); // alias de HomeView (/)
                }
            } else {
                Notification.show("Credenciales inválidas", 2500, Notification.Position.MIDDLE);
            }
        });

        // ----- Wrapper estilizado (usa .login-wrapper del CSS) -----
        VerticalLayout wrapper = new VerticalLayout(title, login);
        wrapper.addClassName("login-wrapper");
        wrapper.setSizeFull();
        wrapper.setSpacing(false);
        wrapper.setPadding(false);
        wrapper.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        add(wrapper);
    }

    private LoginI18n spanishI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getHeader().setTitle("Iniciar sesión");
        i18n.getHeader().setDescription("Introduce tus credenciales");
        i18n.getForm().setUsername("Usuario");
        i18n.getForm().setPassword("Contraseña");
        i18n.getForm().setSubmit("Acceder");
        i18n.getErrorMessage().setTitle("Error de autenticación");
        i18n.getErrorMessage().setMessage("Usuario o contraseña incorrectos.");
        return i18n;
    }
}
