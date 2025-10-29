package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("login")
@PageTitle("Iniciar sesión | FastTasty")
@CssImport("./themes/my-theme/login.css")
public class LoginView extends VerticalLayout {

    public LoginView(UserService userService) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("login-view");

        H1 title = new H1("Fast&Tasty 🍔");
        title.addClassName("login-title-main");

        // 🔹 Login form
        LoginForm login = new LoginForm();
        login.setI18n(spanishI18nSafe());
        login.setForgotPasswordButtonVisible(false);
        login.addLoginListener(e -> {
            final String username = e.getUsername();
            final String rawPassword = e.getPassword();

            if (userService.authenticate(username, rawPassword)) {
                User user = userService.findByUsername(username).orElseThrow();
                VaadinSession.getCurrent().setAttribute(User.class, user);

                if (user.getRole() == Role.ADMIN) {
                    UI.getCurrent().navigate("admin/users");
                } else {
                    UI.getCurrent().navigate("carta");
                }
            } else {
                Notification.show("Credenciales inválidas", 2500, Notification.Position.MIDDLE);
            }
        });

        // 🔹 Texto inferior
        Div registerText = new Div();
        registerText.addClassName("login-register-text");
        registerText.getElement().setProperty(
                "innerHTML",
                "¿No tienes una cuenta aún? <a href='register'>Regístrate</a>"
        );

        // 🔹 Card
        Div card = new Div(login, registerText);
        card.addClassName("login-card");

        // 🔹 Layout principal
        add(title, card);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private LoginI18n spanishI18nSafe() {
        LoginI18n i18n = new LoginI18n();

        LoginI18n.Header header = new LoginI18n.Header();
        header.setTitle("Iniciar sesión");
        header.setDescription("");
        i18n.setHeader(header);

        LoginI18n.Form form = new LoginI18n.Form();
        form.setUsername("Usuario");
        form.setPassword("Contraseña");
        form.setSubmit("Acceder");
        i18n.setForm(form);

        LoginI18n.ErrorMessage error = new LoginI18n.ErrorMessage();
        error.setTitle("Error de autenticación");
        error.setMessage("Usuario o contraseña incorrectos.");
        i18n.setErrorMessage(error);

        return i18n;
    }
}
