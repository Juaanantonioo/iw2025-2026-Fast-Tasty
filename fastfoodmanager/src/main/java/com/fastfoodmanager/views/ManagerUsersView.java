package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;


import java.util.Set;

@PageTitle("Gestión de usuarios | FastTasty")
@Route(value = "manager/users", layout = MainLayout.class)
@RolesAllowed("MANAGER")
public class ManagerUsersView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    private final TextField username = new TextField("Usuario");
    private final PasswordField password = new PasswordField("Contraseña");
    private final TextField telefono = new TextField("Teléfono");
    private final TextField email = new TextField("Email");
    private final TextField direccion = new TextField("Dirección");

    private final ComboBox<Role> role = new ComboBox<>("Rol");

    private static final Set<Role> ALLOWED = Set.of(Role.OPERATOR, Role.COOK, Role.DELIVERY);

    public ManagerUsersView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Gestión de usuarios (Encargado)"));

        username.setRequired(true);
        password.setRequired(true);
        telefono.setRequired(true);
        email.setRequired(true);
        direccion.setRequired(true);

        role.setItems(ALLOWED);
        role.setRequired(true);

        Button create = new Button("Crear usuario", e -> createUser());
        create.getStyle().set("background", "#ff7b00").set("color", "white");

        HorizontalLayout form = new HorizontalLayout(username, password, telefono, email, direccion, role, create);
        form.setDefaultVerticalComponentAlignment(Alignment.END);
        add(form);

        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Rol").setAutoWidth(true);

        grid.addComponentColumn(u -> {
            Button delete = new Button("Eliminar", ev -> {
                if (u.getRole() == Role.ADMIN) {
                    Notification.show("No puedes eliminar ADMIN");
                    return;
                }
                if (u.getRole() == Role.MANAGER) {
                    Notification.show("No puedes eliminar MANAGER (eso es del ADMIN)");
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

    private void createUser() {
        String u = username.getValue() == null ? "" : username.getValue().trim();
        String p = password.getValue() == null ? "" : password.getValue().trim();
        String t = telefono.getValue() == null ? "" : telefono.getValue().trim();
        String e = email.getValue() == null ? "" : email.getValue().trim();
        String d = direccion.getValue() == null ? "" : direccion.getValue().trim();
        Role r = role.getValue();

        if (u.isEmpty() || p.isEmpty() || t.isEmpty() || e.isEmpty() || d.isEmpty() || r == null) {
            Notification.show("Todos los campos son obligatorios");
            return;
        }
        if (!ALLOWED.contains(r)) {
            Notification.show("El encargado solo puede crear OPERATOR / COOK / DELIVERY");
            return;
        }

        try {
            userService.registerUser(u, p, r, t, e, d);
            username.clear(); password.clear(); telefono.clear(); email.clear(); direccion.clear();
            role.clear();
            refresh();
            Notification.show("Usuario creado: " + r.name());
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage());
        } catch (Exception ex) {
            Notification.show("Error creando usuario");
        }
    }

    private void refresh() {
        grid.setItems(userService.findAll().stream()
                .filter(u -> u.getRole() != Role.ADMIN && u.getRole() != Role.MANAGER && u.getRole() != Role.USER)
                .toList());
    }
}
