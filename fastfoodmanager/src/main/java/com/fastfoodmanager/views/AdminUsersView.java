package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Usuarios | FastTasty")
@Route(value = "admin/users", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminUsersView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    private final TextField username = new TextField("Nuevo operador - usuario");
    private final PasswordField password = new PasswordField("Contraseña");

    public AdminUsersView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Título
        add(new H2("Gestión de usuarios"));

        // ---------- Alta de OPERADORES ----------
        username.setClearButtonVisible(true);
        username.setRequired(true);
        username.setMaxLength(30);

        password.setClearButtonVisible(true);
        password.setRequired(true);

        Button addOperator = new Button("Crear operador", e -> createOperator());
        addOperator.getStyle().set("background", "#ff7b00").set("color", "white");

        HorizontalLayout form = new HorizontalLayout(username, password, addOperator);
        form.setDefaultVerticalComponentAlignment(Alignment.END);
        add(form);

        // ---------- Grid de usuarios ----------
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Rol").setAutoWidth(true);

        grid.addComponentColumn(u -> {
            Button delete = new Button("Eliminar", ev -> {
                // Evitamos borrar al admin principal
                if ("admin".equalsIgnoreCase(u.getUsername())) {
                    Notification.show("No se puede eliminar el admin principal");
                    return;
                }
                userService.deleteUser(u.getId());
                refresh();
                Notification.show("Usuario eliminado");
            });
            return delete;
        }).setHeader("Acciones");

        grid.setAllRowsVisible(true);
        add(grid);

        refresh();
    }

    private void createOperator() {
        String u = username.getValue() == null ? "" : username.getValue().trim();
        String p = password.getValue() == null ? "" : password.getValue().trim();

        if (u.isEmpty() || p.isEmpty()) {
            Notification.show("Usuario y contraseña obligatorios");
            return;
        }
        try {
            userService.registerUser(u, p, Role.OPERATOR);
            username.clear();
            password.clear();
            refresh();
            Notification.show("Operador creado");
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage());
        } catch (Exception ex) {
            Notification.show("Error creando operador");
        }
    }

    private void refresh() {
        grid.setItems(userService.findAll());
    }
}
