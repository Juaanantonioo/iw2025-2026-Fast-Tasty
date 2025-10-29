package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vaadin.flow.server.auth.AnonymousAllowed;  // 👈
@Route("register")
@PageTitle("Crear cuenta | FastTasty")
@CssImport("./themes/my-theme/register.css")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    public RegisterView(UserRepository userRepo, PasswordEncoder encoder) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("register-view");

        H1 title = new H1("Crear cuenta");
        title.addClassName("register-title-main");

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
                Notification.show("Rellena todos los campos"); return;
            }
            if (!p.equals(c)) {
                Notification.show("Las contraseñas no coinciden"); return;
            }
            if (userRepo.findByUsername(u).isPresent()) {
                Notification.show("Ese usuario ya existe"); return;
            }

            User newUser = new User();
            newUser.setUsername(u);
            newUser.setPassword(encoder.encode(p)); // BCrypt
            newUser.setRole(Role.USER);

            userRepo.save(newUser);
            Notification.show("Cuenta creada. Inicia sesión.");
            getUI().ifPresent(ui -> ui.navigate("login?registered=1"));
        });
        submit.addClassName("register-button");

        Div loginText = new Div();
        loginText.addClassName("register-login-text");
        loginText.getElement().setProperty(
                "innerHTML", "¿Ya tienes cuenta? <a href='login'>Inicia sesión</a>"
        );

        Div card = new Div(username, password, confirm, submit, loginText);
        card.addClassName("register-card");

        add(title, card);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
}
