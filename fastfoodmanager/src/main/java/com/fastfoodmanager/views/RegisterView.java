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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.vaadin.flow.server.auth.AnonymousAllowed;

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

        TextField telefono = new TextField("Teléfono");
        telefono.setRequired(true);
        telefono.setPlaceholder("Ej: 612345678");

        EmailField email = new EmailField("Email");
        email.setRequiredIndicatorVisible(true);
        email.setPlaceholder("ejemplo@correo.com");

        TextField direccion = new TextField("Dirección");
        direccion.setRequired(true);
        direccion.setPlaceholder("Calle, número, ciudad");

        Button submit = new Button("Crear cuenta", e -> {
            String u = username.getValue().trim();
            String p = password.getValue();
            String c = confirm.getValue();
            String t = telefono.getValue().replaceAll("\\D", ""); // solo dígitos
            String mail = email.getValue().trim();
            String dir = direccion.getValue().trim();

            // Validaciones
            if (u.isEmpty() || p.isEmpty() || c.isEmpty() || t.isEmpty() || mail.isEmpty() || dir.isEmpty()) {
                Notification.show("Rellena todos los campos");
                return;
            }
            if (!p.equals(c)) {
                Notification.show("Las contraseñas no coinciden");
                return;
            }
            if (t.length() != 9) {
                Notification.show("El teléfono debe tener 9 dígitos");
                return;
            }
            if (!mail.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
                Notification.show("Introduce un email válido");
                return;
            }
            if (userRepo.findByUsername(u).isPresent()) {
                Notification.show("Ese usuario ya existe");
                return;
            }

            User newUser = new User();
            newUser.setUsername(u);
            newUser.setPassword(encoder.encode(p));
            newUser.setRole(Role.USER);

            newUser.setTelefono(t);   // Asegúrate de añadir este campo en User
            newUser.setEmail(mail);   // Asegúrate de añadir este campo en User
            newUser.setDireccion(dir);// Asegúrate de añadir este campo en User

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

        Div card = new Div(username, password, confirm, telefono, email, direccion, submit, loginText);
        card.addClassName("register-card");

        add(title, card);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
}
