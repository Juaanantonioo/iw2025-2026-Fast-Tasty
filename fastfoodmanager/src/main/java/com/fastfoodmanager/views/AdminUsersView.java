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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Usuarios | FastTasty")
@Route(value = "admin/users", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminUsersView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    // 🔍 Buscador
    private final TextField searchField = new TextField("Buscar por nombre");

    // 🎚 Filtro por rol
    private final ComboBox<Role> roleFilter = new ComboBox<>("Filtrar por rol");

    public AdminUsersView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Gestión de usuarios"));

        // ====== BUSCADOR ======
        searchField.setPlaceholder("Escribe un nombre...");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refresh());

        // ====== FILTRO POR ROL ======
        roleFilter.setItems(Role.values());
        roleFilter.setPlaceholder("Selecciona rol");
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(e -> refresh());

        HorizontalLayout filters = new HorizontalLayout(searchField, roleFilter);
        filters.setWidthFull();
        filters.setAlignItems(Alignment.END);
        add(filters);

        // ===== GRID =====
        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getUsername).setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Rol").setAutoWidth(true);

        grid.addComponentColumn(u -> {
            Button delete = new Button("Eliminar", ev -> {
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

    private void refresh() {
        List<User> users = userService.findAll();

        // 🔍 Filtro por nombre
        String search = searchField.getValue();
        if (search != null && !search.isBlank()) {
            users = users.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 🎚 Filtro por rol
        Role selectedRole = roleFilter.getValue();
        if (selectedRole != null) {
            users = users.stream()
                    .filter(u -> u.getRole() == selectedRole)
                    .collect(Collectors.toList());
        }

        grid.setItems(users);
    }
}
