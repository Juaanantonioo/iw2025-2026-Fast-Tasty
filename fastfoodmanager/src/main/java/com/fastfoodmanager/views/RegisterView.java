package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Crear cuenta")
@Route("register")
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Crear cuenta");

        TextField username = new TextField("Usuario");
        username.setClearButtonVisible(true);
        username.setMaxLength(30);
        username.setWidth("320px");

        PasswordField pass1 = new PasswordField("Contraseña");
        pass1.setWidth("320px");

        PasswordField pass2 = new PasswordField("Repite la contraseña");
        pass2.setWidth("320px");

        Button create = new Button("Crear cuenta", e -> {
            String u = username.getValue().trim();
            String p1 = pass1.getValue();
            String p2 = pass2.getValue();

            if (u.length() < 3) {
                Notification.show("El usuario debe tener al menos 3 caracteres");
                return;
            }
            if (p1.length() < 4) {
                Notification.show("La contraseña debe tener al menos 4 caracteres");
                return;
            }
            if (!p1.equals(p2)) {
                Notification.show("Las contraseñas no coinciden");
                return;
            }
            if (userService.exists(u)) {
                Notification.show("Ese usuario ya existe");
                return;
            }

            // Crea SIEMPRE con rol USER
            userService.registerCustomer(u, p1);

            Notification.show("Cuenta creada. Inicia sesión.");
            UI.getCurrent().navigate("login");
        });

        add(title, username, pass1, pass2, create);
    }
}
