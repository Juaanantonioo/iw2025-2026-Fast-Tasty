package com.fastfoodmanager.views;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;  // 👈
@Route("login")
@PageTitle("Iniciar sesión | FastTasty")
@CssImport("./themes/my-theme/login.css")
@AnonymousAllowed                                    // 👈 pública de verdad
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("login-view");

        // Título (fuera de la tarjeta)
        H1 title = new H1("Fast&Tasty 🍔");
        title.addClassName("login-title-main");

        // LoginForm -> Spring Security
        login.setAction("login");                // <— MUY IMPORTANTE
        login.setI18n(spanishI18nSafe());
        login.setForgotPasswordButtonVisible(false);

        // Texto inferior (registro)
        Div registerText = new Div();
        registerText.addClassName("login-register-text");
        registerText.getElement().setProperty(
                "innerHTML",
                "¿No tienes una cuenta aún? <a href='register'>Regístrate</a>"
        );

        // Card
        Div card = new Div(login, registerText);
        card.addClassName("login-card");

        add(title, card);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    // Mostrar error si Spring redirige con ?error
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var params = event.getLocation().getQueryParameters().getParameters();
        if (params.containsKey("error")) {
            login.setError(true);
        }
    }

    private LoginI18n spanishI18nSafe() {
        LoginI18n i18n = new LoginI18n();

        LoginI18n.Header header = new LoginI18n.Header();
        header.setTitle("Log in");
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
