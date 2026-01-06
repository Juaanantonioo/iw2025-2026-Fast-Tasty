package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.AllergenService;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "admin/alergenos", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminAllergenView extends VerticalLayout {

    private final AllergenService allergenService;
    private final ProductService productService;

    private Grid<Allergen> grid = new Grid<>(Allergen.class, false);
    private TextField nameField = new TextField("Nombre del alérgeno");

    private Button saveBtn = new Button("Guardar");
    private Button deleteBtn = new Button("Eliminar");
    private Button newBtn = new Button("➕ Nuevo alérgeno");

    private Allergen selected;

    public AdminAllergenView(AllergenService allergenService, ProductService productService) {
        this.allergenService = allergenService;
        this.productService = productService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureButtons();

        HorizontalLayout content = new HorizontalLayout(
                grid,
                createForm()
        );

        content.setSizeFull();
        content.setFlexGrow(1, grid);

        add(
                new H2("Gestión de Alérgenos"),
                content
        );

        refresh();
    }

    // ---------- GRID ----------
    private void configureGrid() {
        grid.addColumn(Allergen::getName).setHeader("Alérgeno");
        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(e -> {
            selected = e.getValue();

            if (selected != null) {
                nameField.setValue(selected.getName());
                deleteBtn.setEnabled(true);
            }
        });
    }

    // ---------- FORM ----------
    private VerticalLayout createForm() {

        VerticalLayout form = new VerticalLayout(
                newBtn,
                nameField,
                saveBtn,
                deleteBtn
        );

        form.setWidth("320px");
        form.setPadding(true);
        form.setSpacing(true);
        form.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");

        return form;
    }

    private void configureButtons() {
        newBtn.addClickListener(e -> newAllergen());

        saveBtn.addClickListener(e -> save());

        deleteBtn.addClickListener(e -> confirmDelete());
        deleteBtn.setEnabled(false);
    }

    // ---------- ACTIONS ----------
    private void newAllergen() {
        grid.deselectAll();
        selected = null;
        nameField.clear();
        deleteBtn.setEnabled(false);
        nameField.focus();
    }

    private void save() {
        if (nameField.isEmpty()) {
            Notification.show("El nombre es obligatorio");
            return;
        }

        if (selected == null) {
            selected = new Allergen();
        }

        selected.setName(nameField.getValue());

        allergenService.save(selected);

        refresh();
        newAllergen();
        Notification.show("Alérgeno guardado");
    }

    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar alérgeno");
        dialog.setText(
                "⚠️ Este alérgeno será eliminado de todos los productos.\n\n¿Deseas continuar?"
        );

        dialog.setCancelable(true);
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            removeAllergenFromProducts(selected);
            allergenService.delete(selected);
            newAllergen();
            refresh();
            Notification.show("Alérgeno eliminado");
        });

        dialog.open();
    }

    // ---------- REMOVE ALLERGEN FROM PRODUCTS ----------
    private void removeAllergenFromProducts(Allergen allergen) {
        List<Product> products = productService.findAll();

        for (Product p : products) {
            if (p.getAllergens() != null && p.getAllergens().contains(allergen)) {
                p.getAllergens().remove(allergen);
                productService.save(p);
            }
        }
    }

    // ---------- UTILS ----------
    private void refresh() {
        grid.setItems(allergenService.findAll());
    }
}
