package com.fastfoodmanager.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("register")
@PageTitle("Crear cuenta | FastTasty")
@CssImport("./themes/my-theme/register.css")
public class RegisterView extends VerticalLayout {

    public RegisterView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("register-view");

        // Título arriba (fuera de la tarjeta)
        H1 title = new H1("Crear cuenta");
        title.addClassName("register-title-main");

        // Campos
        TextField username = new TextField("Usuario");
        username.setRequired(true);

        PasswordField password = new PasswordField("Contraseña");
        password.setRequired(true);

        PasswordField confirm = new PasswordField("Repite la contraseña");
        confirm.setRequired(true);

        Button submit = new Button("Crear cuenta", e -> {
            String u = username.getValue().trim();
            String p = password.getValue();
            String c = confirm.getValue();

            if (u.isEmpty() || p.isEmpty() || c.isEmpty()) {
                Notification.show("Rellena todos los campos");
                return;
            }
            if (!p.equals(c)) {
                Notification.show("Las contraseñas no coinciden");
                return;
            }

            // TODO: Llama aquí a tu servicio real de registro, por ejemplo:
            // userService.register(u, p);
            // y maneja el caso de usuario existente, etc.

            Notification.show("Cuenta creada. Ya puedes iniciar sesión.");
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        submit.addClassName("register-button");

        // Enlace inferior
        Div loginText = new Div();
        loginText.addClassName("register-login-text");
        loginText.getElement().setProperty(
                "innerHTML",
                "¿Ya tienes cuenta? <a href='login'>Inicia sesión</a>"
        );

        // Tarjeta
        Div card = new Div(username, password, confirm, submit, loginText);
        card.addClassName("register-card");

        add(title, card);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
}
