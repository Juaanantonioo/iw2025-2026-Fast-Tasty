package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.repository.AllergenRepository;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.stream.Collectors;

@Route(value = "products", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class ProductView extends VerticalLayout {

    private final ProductService service;
    private final AllergenRepository allergenRepo;

    // Formulario
    private final TextField name = new TextField("Nombre");
    private final TextArea description = new TextArea("Descripción");
    private final ComboBox<FoodType> type = new ComboBox<>("Tipo");
    private final MultiSelectComboBox<Allergen> allergens = new MultiSelectComboBox<>("Alérgenos");
    private final NumberField price = new NumberField("Precio (€)");
    private final com.vaadin.flow.component.checkbox.Checkbox active = new com.vaadin.flow.component.checkbox.Checkbox("Activo", true);

    // Botones
    private final Button save = new Button("Guardar");
    private final Button clear = new Button("Limpiar");
    private final Button delete = new Button("Eliminar seleccionado");

    // Grid
    private final Grid<Product> grid = new Grid<>(Product.class, false);

    private final Binder<Product> binder = new Binder<>(Product.class);
    private Product current = new Product();

    public ProductView(ProductService service, AllergenRepository allergenRepo) {
        this.service = service;
        this.allergenRepo = allergenRepo;

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        addClassName("products-admin-view");

        // Cabecera
        var header = new H1("Gestión de productos");
        header.getStyle()
                .set("margin", "0")
                .set("font-weight", "800")
                .set("color", "#1f2937");

        // Botones estilizados
        stylePrimary(save);
        styleTertiary(clear);
        styleError(delete);

        HorizontalLayout actionsBar = new HorizontalLayout(save, clear, delete);
        actionsBar.setAlignItems(Alignment.CENTER);
        actionsBar.getStyle().set("gap", "10px");

        // ===== FORMULARIO =====
        description.setMaxLength(800);
        description.setHelperText("Máx. 800 caracteres");
        description.setWidthFull();

        price.setStep(0.10);
        price.setMin(0.0);
        price.setClearButtonVisible(true);
        price.setWidth("180px");

        name.setClearButtonVisible(true);
        name.setWidth("280px");

        // ComboBox FoodType
        type.setItems(service.findAllFoodTypes());
        type.setItemLabelGenerator(FoodType::getName);
        type.setPlaceholder("Selecciona el tipo");

        // MultiSelect alérgenos
        allergens.setItems(allergenRepo.findAll());
        allergens.setItemLabelGenerator(Allergen::getName);
        allergens.setPlaceholder("Selecciona alérgenos si los tiene");

        // FormLayout
        var form = new com.vaadin.flow.component.formlayout.FormLayout();
        form.setResponsiveSteps(
                new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("0", 1),
                new com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep("720px", 2)
        );
        form.add(name, type, price, description, allergens, active);
        form.setColspan(description, 2);
        form.setColspan(allergens, 2);

        Div formCard = new Div(form, actionsBar);
        formCard.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "16px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,.06)")
                .set("margin-bottom", "14px");

        // ===== GRID =====
        grid.addColumn(Product::getId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Product::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(p -> p.getType() != null ? p.getType().getName() : "")
                .setHeader("Tipo")
                .setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Descripción").setAutoWidth(true).setFlexGrow(2);
        grid.addColumn(p -> formatMoney(p.getPrice())).setHeader("Precio").setAutoWidth(true);
        grid.addColumn(p -> p.getAllergens() == null ? "" :
                        p.getAllergens().stream()
                                .map(Allergen::getName)
                                .collect(Collectors.joining(", ")))
                .setHeader("Alérgenos")
                .setAutoWidth(true);
        grid.addColumn(Product::isActive).setHeader("Activo").setAutoWidth(true);

        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        grid.setHeight("55vh");

        grid.asSingleSelect().addValueChangeListener(ev -> {
            var sel = ev.getValue();
            if (sel != null) {
                current = sel;
                binder.readBean(current);
            } else {
                resetForm();
            }
        });

        // Layout principal
        add(header, formCard, grid);

        // ===== BINDER =====
        binder.forField(name)
                .asRequired("El nombre es obligatorio")
                .bind(Product::getName, Product::setName);

        binder.forField(description)
                .bind(Product::getDescription, Product::setDescription);

        binder.forField(type)
                .asRequired("Debes elegir un tipo de comida")
                .bind(Product::getType, Product::setType);

        binder.forField(allergens)
                .bind(Product::getAllergens, Product::setAllergens);

        binder.forField(price)
                .asRequired("El precio es obligatorio")
                .withValidator(new DoubleRangeValidator("El precio debe ser ≥ 0", 0.0, null))
                .bind(Product::getPrice, Product::setPrice);

        binder.forField(active)
                .bind(Product::isActive, Product::setActive);

        // ===== EVENTOS =====
        save.addClickListener(e -> onSave());
        clear.addClickListener(e -> resetForm());
        delete.addClickListener(e -> {
            var sel = grid.asSingleSelect().getValue();
            if (sel == null) {
                Notification.show("Selecciona una fila para eliminar");
                return;
            }
            service.delete(sel.getId());
            Notification.show("Producto eliminado");
            load();
            resetForm();
        });

        load();
    }

    private void onSave() {
        try {
            binder.writeBean(current);
            service.save(current);
            Notification.show("Producto guardado");
            load();
            current = new Product();
            binder.readBean(current);
        } catch (ValidationException ex) {
            Notification.show("Revisa el formulario");
        }
    }

    private void load() {
        grid.setItems(service.findAll());
    }

    private void resetForm() {
        current = new Product();
        binder.readBean(current);
        grid.deselectAll();
    }

    // Helpers

    private static String formatMoney(Double value) {
        if (value == null) return "";
        return String.format("%.2f €", value);
    }

    private static void stylePrimary(Button b) {
        b.getStyle().set("background", "#ff6a1a")
                .set("color", "white")
                .set("font-weight", "700")
                .set("border-radius", "10px");
    }

    private static void styleTertiary(Button b) {
        b.getStyle().set("background", "#f3f4f6")
                .set("color", "#111827")
                .set("border-radius", "10px");
    }

    private static void styleError(Button b) {
        b.getStyle().set("background", "#fee2e2")
                .set("color", "#991b1b")
                .set("border-radius", "10px");
    }
}
