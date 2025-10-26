package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.VaadinSession;

@PageTitle("Administración de usuarios")
@Route(value = "admin/users", layout = MainLayout.class)
public class AdminUsersView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public AdminUsersView(UserService userService) {
        this.userService = userService;

        // 🔐 Protección mínima por sesión: solo ADMIN
        User current = VaadinSession.getCurrent().getAttribute(User.class);
        if (current == null || current.getRole() != Role.ADMIN) {
            Notification.show("Debes ser ADMIN");
            UI.getCurrent().navigate("login");
            return;
        }

        setWidthFull();
        setMaxWidth("900px");
        setAlignItems(Alignment.STRETCH);
        add(new H2("👑 Administración de usuarios"));

        // ----- Formulario de alta -----
        TextField username = new TextField("Usuario");
        username.setClearButtonVisible(true);

        PasswordField password = new PasswordField("Contraseña");
        password.setClearButtonVisible(true);

        ComboBox<Role> role = new ComboBox<>("Rol");
        role.setItems(Role.values());
        role.setValue(Role.OPERATOR);

        Button add = new Button("Crear usuario", e -> {
            try {
                userService.registerUser(username.getValue().trim(),
                        password.getValue(),
                        role.getValue());
                username.clear(); password.clear(); role.setValue(Role.OPERATOR);
                refresh();
                Notification.show("Usuario creado");
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            }
        });

        var form = new HorizontalLayout(username, password, role, add);
        form.setDefaultVerticalComponentAlignment(Alignment.END);
        add(form);

        // ----- Grid -----
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Rol").setAutoWidth(true);

        // Cambiar rol desde el grid (botones)
        grid.addComponentColumn(u -> {
            ComboBox<Role> cb = new ComboBox<>();
            cb.setItems(Role.values());
            cb.setValue(u.getRole());
            cb.addValueChangeListener(ev -> {
                userService.changeRole(u.getId(), ev.getValue());
                refresh();
                Notification.show("Rol actualizado");
            });
            return cb;
        }).setHeader("Cambiar rol");

        add(grid);

        refresh();
    }

    private void refresh() {
        grid.setItems(userService.findAll());
    }
}
