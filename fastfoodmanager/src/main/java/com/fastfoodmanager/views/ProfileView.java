package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("profile")
@PageTitle("Mi Perfil | FastTasty")
@AnonymousAllowed
public class ProfileView extends VerticalLayout implements BeforeEnterObserver { // <-- Implementa la interfaz

    private final UserService userService;
    private User currentUser;

    private TextField usernameField;
    private TextField telefonoField;
    private EmailField emailField;
    private TextField direccionField;
    private TextField roleField;

    public ProfileView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("profile-view");

        H1 title = new H1("Mi Perfil");
        title.addClassName("profile-title");

        // Cargar datos del usuario actual
        loadUserData();

        if (currentUser == null) {
            add(new H3("No se pudo cargar la información del usuario"));
            return;
        }

        // Crear formulario de visualización
        usernameField = new TextField("Usuario");
        usernameField.setValue(currentUser.getUsername());
        usernameField.setReadOnly(true);
        usernameField.setWidth("100%");

        roleField = new TextField("Rol");
        roleField.setValue(currentUser.getRole().toString());
        roleField.setReadOnly(true);
        roleField.setWidth("100%");

        telefonoField = new TextField("Teléfono");
        telefonoField.setValue(currentUser.getTelefono());
        telefonoField.setWidth("100%");

        emailField = new EmailField("Email");
        emailField.setValue(currentUser.getEmail());
        emailField.setWidth("100%");

        direccionField = new TextField("Dirección");
        direccionField.setValue(currentUser.getDireccion());
        direccionField.setWidth("100%");

        // Botones
        Button saveButton = new Button("Guardar cambios", e -> saveChanges());
        saveButton.addClassName("profile-save-button");

        Button editButton = new Button("Editar perfil", e -> setEditable(true));
        editButton.addClassName("profile-edit-button");

        Button cancelButton = new Button("Cancelar", e -> {
            loadUserData();
            setEditable(false);
        });
        cancelButton.addClassName("profile-cancel-button");

        // Inicialmente en modo lectura
        setEditable(false);

        VerticalLayout formLayout = new VerticalLayout(
                usernameField,
                roleField,
                telefonoField,
                emailField,
                direccionField
        );
        formLayout.setSpacing(true);
        formLayout.setWidth("500px");
        formLayout.addClassName("profile-form");

        add(title, formLayout, editButton);
    }

    @Override  // <-- AÑADE ESTA ANOTACIÓN @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verifica manualmente si el usuario está autenticado
        String username = userService.getCurrentUsername();
        if (username == null) {
            // No está autenticado, redirige a login
            event.forwardTo(LoginView.class);
        } else {
            // Está autenticado, carga los datos
            currentUser = userService.findByUsername(username).orElse(null);
            if (currentUser == null) {
                event.forwardTo(LoginView.class);
            }
        }
    }

    private void loadUserData() {
        String username = userService.getCurrentUsername();
        if (username != null) {
            currentUser = userService.findByUsername(username).orElse(null);
        }
    }

    private void setEditable(boolean editable) {
        telefonoField.setReadOnly(!editable);
        emailField.setReadOnly(!editable);
        direccionField.setReadOnly(!editable);

        // Cambiar botones según el modo
        removeAll();

        H1 title = new H1("Mi Perfil");
        title.addClassName("profile-title");

        VerticalLayout formLayout = new VerticalLayout(
                usernameField,
                roleField,
                telefonoField,
                emailField,
                direccionField
        );
        formLayout.setSpacing(true);
        formLayout.setWidth("500px");
        formLayout.addClassName("profile-form");

        if (editable) {
            Button saveButton = new Button("Guardar cambios", e -> saveChanges());
            saveButton.addClassName("profile-save-button");

            Button cancelButton = new Button("Cancelar", e -> {
                loadUserData();
                telefonoField.setValue(currentUser.getTelefono());
                emailField.setValue(currentUser.getEmail());
                direccionField.setValue(currentUser.getDireccion());
                setEditable(false);
            });
            cancelButton.addClassName("profile-cancel-button");

            add(title, formLayout, saveButton, cancelButton);
        } else {
            Button editButton = new Button("Editar perfil", e -> setEditable(true));
            editButton.addClassName("profile-edit-button");

            add(title, formLayout, editButton);
        }
    }

    private void saveChanges() {
        // Validaciones
        String telefono = telefonoField.getValue().replaceAll("\\D", "");
        String email = emailField.getValue().trim();
        String direccion = direccionField.getValue().trim();

        if (telefono.isEmpty() || email.isEmpty() || direccion.isEmpty()) {
            Notification.show("Todos los campos son obligatorios");
            return;
        }

        if (telefono.length() != 9) {
            Notification.show("El teléfono debe tener 9 dígitos");
            return;
        }

        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            Notification.show("Introduce un email válido");
            return;
        }

        // Actualizar usuario
        currentUser.setTelefono(telefono);
        currentUser.setEmail(email);
        currentUser.setDireccion(direccion);

        userService.updateUser(currentUser);

        Notification.show("Perfil actualizado correctamente");
        setEditable(false);
    }
}