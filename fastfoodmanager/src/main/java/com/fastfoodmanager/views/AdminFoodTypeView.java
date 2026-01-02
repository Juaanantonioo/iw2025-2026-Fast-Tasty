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
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.service.FoodTypeService;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

@Route("admin/categorias")
@RolesAllowed("ADMIN")
public class AdminFoodTypeView extends VerticalLayout {

    private final FoodTypeService foodTypeService;

    private Grid<FoodType> grid = new Grid<>(FoodType.class, false);
    private TextField nameField = new TextField("Nombre");
    private Button saveBtn = new Button("Guardar");
    private Button deleteBtn = new Button("Eliminar");

    private FoodType selected;

    public AdminFoodTypeView(FoodTypeService foodTypeService) {
        this.foodTypeService = foodTypeService;

        setSizeFull();
        setPadding(true);

        configureGrid();
        configureForm();

        add(
                new H2("Gestión de Categorías"),
                new HorizontalLayout(grid, createForm())
        );

        refresh();
    }

    private void configureGrid() {
        grid.addColumn(FoodType::getName).setHeader("Nombre");
        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(e -> {
            selected = e.getValue();
            if (selected != null) {
                nameField.setValue(selected.getName());
                deleteBtn.setEnabled(true);
            }
        });
    }

    private void configureForm() {
        saveBtn.addClickListener(e -> save());
        deleteBtn.addClickListener(e -> confirmDelete());
        deleteBtn.setEnabled(false);
    }

    private VerticalLayout createForm() {
        VerticalLayout form = new VerticalLayout(nameField, saveBtn, deleteBtn);
        form.setWidth("300px");
        return form;
    }

    private void save() {
        if (nameField.isEmpty()) {
            Notification.show("El nombre es obligatorio");
            return;
        }

        if (selected == null) {
            selected = new FoodType();
        }

        selected.setName(nameField.getValue());
        foodTypeService.save(selected);

        clearForm();
        refresh();
        Notification.show("Categoría guardada");
    }

    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar categoría");
        dialog.setText(
                "⚠️ Se eliminarán todos los productos de esta categoría.\n¿Deseas continuar?"
        );

        dialog.setCancelable(true);
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            foodTypeService.deleteFoodType(selected);
            clearForm();
            refresh();
            Notification.show("Categoría eliminada");
        });

        dialog.open();
    }

    private void refresh() {
        grid.setItems(foodTypeService.findAll());
    }

    private void clearForm() {
        nameField.clear();
        selected = null;
        deleteBtn.setEnabled(false);
        grid.deselectAll();
    }
}
