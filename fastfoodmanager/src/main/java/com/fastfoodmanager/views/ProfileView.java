package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
@CssImport("./themes/my-theme/profile.css")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private User currentUser;

    private TextField usernameField;
    private TextField telefonoField;
    private EmailField emailField;
    private TextField direccionField;
    private TextField roleField;

    // Botones
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;

    // Layouts
    private VerticalLayout mainContentLayout;
    private HorizontalLayout buttonLayout;
    private Div profileCard;
    private Button backButton;

    public ProfileView(UserService userService) {
        this.userService = userService;

        // Configuración para ocupar toda la pantalla
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        setPadding(false);
        setSpacing(false);
        addClassName("profile-view");

        // Crear estructura vacía
        createEmptyStructure();
    }

    private void createEmptyStructure() {
        // Header con tres secciones: Botón | Título Centrado | Espacio
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName("profile-header");

        // Botón Volver (izquierda)
        backButton = new Button("Volver", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClickListener(e -> UI.getCurrent().navigate(""));
        backButton.addClassName("back-button");

        // Título "Mi Perfil" (centrado)
        H1 title = new H1("Mi Perfil");
        title.addClassName("profile-title");

        // Contenedor para el título que realmente lo centra
        Div titleContainer = new Div(title);
        titleContainer.addClassName("title-container");

        // Espaciador a la derecha para balancear con el botón izquierdo
        Div rightSpacer = new Div();
        rightSpacer.setWidth("120px"); // Mismo ancho que el botón
        rightSpacer.addClassName("header-spacer");

        // Añadir los tres elementos
        header.add(backButton, titleContainer, rightSpacer);

        // Añadir header directamente
        add(header);

        // Contenedor principal que ocupa todo el espacio restante
        mainContentLayout = new VerticalLayout();
        mainContentLayout.setSizeFull();
        mainContentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        mainContentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        mainContentLayout.setPadding(false);
        mainContentLayout.setSpacing(false);
        mainContentLayout.addClassName("main-content");

        add(mainContentLayout);
        expand(mainContentLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String username = userService.getCurrentUsername();
        if (username == null) {
            event.forwardTo("login");
            return;
        }

        currentUser = userService.findByUsername(username).orElse(null);
        if (currentUser == null) {
            event.forwardTo("login");
            return;
        }

        createProfileView();
    }

    private void createProfileView() {
        // Limpiar contenido previo
        mainContentLayout.removeAll();

        if (currentUser == null) {
            mainContentLayout.add(new H3("No se pudo cargar la información del usuario"));
            return;
        }

        // Crear tarjeta principal
        profileCard = new Div();
        profileCard.addClassName("profile-card");

        // Cabecera de la tarjeta - completamente centrada
        Div cardHeader = new Div();
        cardHeader.addClassName("card-header");

        Icon userIcon = new Icon(VaadinIcon.USER_CARD);
        userIcon.addClassName("user-icon");

        Div cardTitle = new Div();
        cardTitle.setText("Información del Perfil");
        cardTitle.addClassName("card-title");

        // Contenedor vertical para icono y título centrados
        VerticalLayout headerContent = new VerticalLayout();
        headerContent.setPadding(false);
        headerContent.setSpacing(false);
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        headerContent.setWidthFull();
        headerContent.add(userIcon, cardTitle);

        cardHeader.add(headerContent);

        // Crear formulario y botones
        createFormFields();
        createButtons();

        // Layout del formulario
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.addClassName("form-layout");
        formLayout.setSpacing(true);
        formLayout.setWidthFull();
        formLayout.setPadding(false);

        // Campos
        formLayout.add(
                createFullWidthField("Usuario", usernameField),
                createFullWidthField("Rol", roleField),
                createFullWidthField("Teléfono", telefonoField),
                createFullWidthField("Email", emailField),
                createFullWidthField("Dirección", direccionField)
        );

        // Layout de botones
        buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonLayout.setSpacing(true);

        // Inicialmente solo mostrar botón Editar
        buttonLayout.add(editButton);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        // Agregar todo a la tarjeta
        profileCard.add(cardHeader, formLayout, buttonLayout);

        // Contenedor para centrar la tarjeta
        VerticalLayout cardContainer = new VerticalLayout();
        cardContainer.setPadding(false);
        cardContainer.setSpacing(false);
        cardContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        cardContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        cardContainer.setSizeFull();
        cardContainer.add(profileCard);

        mainContentLayout.add(cardContainer);
        mainContentLayout.expand(cardContainer);

        // Iniciar en modo lectura
        setEditable(false);
    }

    private Div createFullWidthField(String label, TextField field) {
        Div fieldContainer = new Div();
        fieldContainer.addClassName("field-container");
        fieldContainer.setWidthFull();

        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.addClassName("field-label");

        field.setWidthFull();
        fieldContainer.add(labelDiv, field);
        return fieldContainer;
    }

    private Div createFullWidthField(String label, EmailField field) {
        Div fieldContainer = new Div();
        fieldContainer.addClassName("field-container");
        fieldContainer.setWidthFull();

        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.addClassName("field-label");

        field.setWidthFull();
        fieldContainer.add(labelDiv, field);
        return fieldContainer;
    }

    private void createFormFields() {
        usernameField = new TextField();
        usernameField.setValue(currentUser.getUsername());
        usernameField.setReadOnly(true);
        usernameField.setWidthFull();
        usernameField.addClassName("readonly-field");

        roleField = new TextField();
        roleField.setValue(currentUser.getRole().toString());
        roleField.setReadOnly(true);
        roleField.setWidthFull();
        roleField.addClassName("readonly-field");

        telefonoField = new TextField();
        telefonoField.setValue(currentUser.getTelefono() != null ? currentUser.getTelefono() : "");
        telefonoField.setPlaceholder("612345678");
        telefonoField.setWidthFull();
        telefonoField.setReadOnly(true);
        telefonoField.addClassName("readonly-field");

        emailField = new EmailField();
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setPlaceholder("ejemplo@dominio.com");
        emailField.setWidthFull();
        emailField.setReadOnly(true);
        emailField.addClassName("readonly-field");

        direccionField = new TextField();
        direccionField.setValue(currentUser.getDireccion() != null ? currentUser.getDireccion() : "");
        direccionField.setPlaceholder("Calle, número, ciudad");
        direccionField.setWidthFull();
        direccionField.setReadOnly(true);
        direccionField.addClassName("readonly-field");
    }

    private void createButtons() {
        editButton = new Button("Editar perfil", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> setEditable(true));
        editButton.addClassName("edit-button");

        saveButton = new Button("Guardar cambios", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveButton.addClickListener(e -> saveChanges());
        saveButton.addClassName("save-button");

        cancelButton = new Button("Cancelar", new Icon(VaadinIcon.CLOSE));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> cancelChanges());
        cancelButton.addClassName("cancel-button");
    }

    private void setEditable(boolean editable) {
        telefonoField.setReadOnly(!editable);
        emailField.setReadOnly(!editable);
        direccionField.setReadOnly(!editable);

        if (editable) {
            telefonoField.removeClassName("readonly-field");
            emailField.removeClassName("readonly-field");
            direccionField.removeClassName("readonly-field");
            telefonoField.addClassName("editable-field");
            emailField.addClassName("editable-field");
            direccionField.addClassName("editable-field");
            profileCard.addClassName("editing-mode");
        } else {
            telefonoField.removeClassName("editable-field");
            emailField.removeClassName("editable-field");
            direccionField.removeClassName("editable-field");
            telefonoField.addClassName("readonly-field");
            emailField.addClassName("readonly-field");
            direccionField.addClassName("readonly-field");
            profileCard.removeClassName("editing-mode");
        }

        buttonLayout.removeAll();
        if (editable) {
            buttonLayout.add(saveButton, cancelButton);
            saveButton.setVisible(true);
            cancelButton.setVisible(true);
        } else {
            buttonLayout.add(editButton);
        }
    }

    private void cancelChanges() {
        if (currentUser != null) {
            telefonoField.setValue(currentUser.getTelefono() != null ? currentUser.getTelefono() : "");
            emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            direccionField.setValue(currentUser.getDireccion() != null ? currentUser.getDireccion() : "");
        }
        setEditable(false);
    }

    private void saveChanges() {
        String telefono = telefonoField.getValue().replaceAll("\\D", "");
        String email = emailField.getValue().trim();
        String direccion = direccionField.getValue().trim();

        if (telefono.isEmpty() || email.isEmpty() || direccion.isEmpty()) {
            showNotification("Todos los campos son obligatorios", false);
            return;
        }

        if (telefono.length() != 9) {
            showNotification("El teléfono debe tener 9 dígitos", false);
            return;
        }

        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            showNotification("Introduce un email válido", false);
            return;
        }

        currentUser.setTelefono(telefono);
        currentUser.setEmail(email);
        currentUser.setDireccion(direccion);

        userService.updateUser(currentUser);

        showNotification("Perfil actualizado correctamente", true);
        setEditable(false);
    }

    private void showNotification(String message, boolean success) {
        Notification notification = Notification.show(message, 3000,
                Notification.Position.MIDDLE);

        if (success) {
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}