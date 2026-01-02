package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.service.TwoFactorService;
import com.fastfoodmanager.service.UserService;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Route("login")
@PageTitle("Iniciar sesión | FastTasty")
@CssImport("./themes/my-theme/login.css")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    private final AuthenticationManager authManager;
    private final TwoFactorService twoFactorService;
    private final UserService userService;

    // Repositorio para guardar la sesión manualmente
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public LoginView(AuthenticationManager authManager,
                     TwoFactorService twoFactorService,
                     UserService userService) {

        this.authManager = authManager;
        this.twoFactorService = twoFactorService;
        this.userService = userService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("login-view");

        H1 title = new H1("Fast&Tasty 🍔");
        title.addClassName("login-title-main");

        login.addLoginListener(e -> {
            try {
                intentarLogin(e.getUsername(), e.getPassword());
            } catch (AuthenticationException ex) {
                login.setError(true);
                login.setEnabled(true);
            }
        });

        login.setI18n(spanishI18nSafe());
        login.setForgotPasswordButtonVisible(false);

        Div registerText = new Div();
        registerText.addClassName("login-register-text");
        registerText.getElement().setProperty("innerHTML", "¿No tienes una cuenta aún? <a href='register'>Regístrate</a>");

        Div card = new Div(login, registerText);
        card.addClassName("login-card");

        add(title, card);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private void intentarLogin(String username, String password) {
        Authentication authAttempt = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authResult = authManager.authenticate(authAttempt);
        User user = userService.findByUsername(username).orElse(null);

        login.setEnabled(true);

        if (user != null) {
            if (user.getSecret2fa() != null && !user.getSecret2fa().isEmpty()) {
                mostrarDialogoVerificacion(user.getSecret2fa(), authResult);
            } else {
                mostrarDialogoConfiguracionObligatoria(user, authResult);
            }
        }
    }

    private void mostrarDialogoVerificacion(String secret, Authentication authResult) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seguridad 2FA Requerida");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(new Span("Introduce el código de tu app autenticadora:"));

        TextField codeField = new TextField();
        codeField.setPlaceholder("Ej: 123456");
        codeField.setAutofocus(true);

        Button verifyButton = new Button("Entrar", e -> {
            validarYEntrar(secret, codeField.getValue(), authResult, dialog, codeField);
        });
        verifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        verifyButton.addClickShortcut(Key.ENTER);

        Button cancelButton = new Button("Cancelar", e -> {
            dialog.close();
            login.setEnabled(true);
            login.setError(false);
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialogLayout.add(codeField);
        dialog.add(dialogLayout);
        dialog.getFooter().add(cancelButton, verifyButton);
        dialog.open();
    }

    private void mostrarDialogoConfiguracionObligatoria(User user, Authentication authResult) {
        String tempSecret = twoFactorService.generateNewSecret();
        String qrUrl = twoFactorService.getQRCodeUrl("FastTasty", user.getEmail(), tempSecret);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("⚠️ Configuración 2FA Obligatoria");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        Image qrImage = new Image(qrUrl, "QR Code");
        qrImage.setWidth("200px");
        qrImage.setHeight("200px");

        TextField codeField = new TextField("Escribe el código para confirmar");
        codeField.setPlaceholder("Ej: 123456");

        Button confirmBtn = new Button("Activar y Entrar", e -> {
            try {
                int code = Integer.parseInt(codeField.getValue());
                if (twoFactorService.validateCode(tempSecret, code)) {
                    user.setSecret2fa(tempSecret);
                    userService.updateUser(user);
                    dialog.close();
                    completarLogin(authResult);
                } else {
                    codeField.setInvalid(true);
                    codeField.setErrorMessage("Código incorrecto");
                }
            } catch (NumberFormatException ex) {
                codeField.setInvalid(true);
                codeField.setErrorMessage("Solo números");
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> {
            dialog.close();
            login.setEnabled(true);
            login.setError(false);
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        layout.add(
                new Paragraph("Por seguridad, debes activar el doble factor."),
                new Paragraph("1. Escanea este QR con Google Authenticator."),
                qrImage,
                new Paragraph("2. Introduce el código generado:"),
                codeField
        );

        dialog.add(layout);
        dialog.getFooter().add(cancelButton, confirmBtn);
        dialog.open();
    }

    private void validarYEntrar(String secret, String inputCode, Authentication authResult, Dialog dialog, TextField field) {
        try {
            int code = Integer.parseInt(inputCode);
            if (twoFactorService.validateCode(secret, code)) {
                dialog.close();
                completarLogin(authResult);
            } else {
                field.setInvalid(true);
                field.setErrorMessage("Código incorrecto");
            }
        } catch (NumberFormatException ex) {
            field.setInvalid(true);
            field.setErrorMessage("Solo números");
        }
    }

    private void completarLogin(Authentication authResult) {
        // 1. Crear contexto de seguridad
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        // 2. GUARDAR EXPLÍCITAMENTE EN LA SESIÓN HTTP
        // Esto es lo que faltaba para que no te pida login al navegar
        VaadinServletRequest request = (VaadinServletRequest) VaadinService.getCurrentRequest();
        VaadinServletResponse response = (VaadinServletResponse) VaadinService.getCurrentResponse();
        securityContextRepository.saveContext(context, request, response);

        // 3. Redirigir según rol
        if (tieneRol(authResult, "ROLE_ADMIN")) {
            UI.getCurrent().navigate("admin/users");
        } else if (tieneRol(authResult, "ROLE_MANAGER")) {
            UI.getCurrent().navigate("manager/users");
        } else if (tieneRol(authResult, "ROLE_OPERATOR")) {
            UI.getCurrent().navigate("operator/orders");
        } else if (tieneRol(authResult, "ROLE_COOK")) {
            UI.getCurrent().navigate("cook/orders");
        } else if (tieneRol(authResult, "ROLE_DELIVERY")) {
            UI.getCurrent().navigate("delivery/orders");
        } else {
            UI.getCurrent().navigate("carta");
        }
    }

    private boolean tieneRol(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }

    private LoginI18n spanishI18nSafe() {
        LoginI18n i18n = new LoginI18n();
        LoginI18n.Header header = new LoginI18n.Header();
        header.setTitle("Log in");
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