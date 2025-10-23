package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.ProductService; // <- importante
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.router.Route;

@Route("products")
public class ProductView extends VerticalLayout {
    private final ProductService service;

    private final TextField name = new TextField("Name");
    private final TextField description = new TextField("Description");
    private final NumberField price = new NumberField("Price");
    private final Checkbox active = new Checkbox("Active", true);

    private final Button save = new Button("Save");
    private final Button clear = new Button("Clear");
    private final Button delete = new Button("Delete selected");

    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private final Binder<Product> binder = new Binder<>(Product.class);
    private Product current = new Product();

    public ProductView(ProductService service) {
        this.service = service;

        add(new H1("Gestión de productos"));
        price.setStep(0.10); price.setMin(0.0);

        var actions = new HorizontalLayout(save, clear, delete);
        var form = new HorizontalLayout(name, description, price, active, actions);
        form.setAlignItems(Alignment.END);

        grid.addColumn(Product::getId).setHeader("Id").setAutoWidth(true);
        grid.addColumn(Product::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addColumn(Product::getPrice).setHeader("Price").setAutoWidth(true);
        grid.addColumn(Product::isActive).setHeader("Active").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.asSingleSelect().addValueChangeListener(ev -> {
            var sel = ev.getValue();
            if (sel != null) { current = sel; binder.readBean(current); }
            else resetForm();
        });

        add(form, grid);
        setSizeFull();

        binder.forField(name).asRequired("Name is required")
                .bind(Product::getName, Product::setName);
        binder.forField(description)
                .bind(Product::getDescription, Product::setDescription);
        binder.forField(price).asRequired("Price is required")
                .withValidator(new DoubleRangeValidator("Price must be >= 0", 0.0, null))
                .bind(Product::getPrice, Product::setPrice);
        binder.forField(active)
                .bind(Product::isActive, Product::setActive);

        save.addClickListener(e -> onSave());
        clear.addClickListener(e -> resetForm());
        delete.addClickListener(e -> {
            var sel = grid.asSingleSelect().getValue();
            if (sel == null) { Notification.show("Select a row to delete"); return; }
            service.delete(sel.getId());
            Notification.show("Deleted"); load(); resetForm();
        });

        load();
    }

    private void onSave() {
        try {
            binder.writeBean(current);
            service.save(current);
            Notification.show("Saved");
            load();
            current = new Product();
            binder.readBean(current);
        } catch (ValidationException ex) {
            Notification.show("Please fix the form");
        }
    }

    private void load() { grid.setItems(service.findAll()); }
    private void resetForm() { current = new Product(); binder.readBean(current); grid.deselectAll(); }
}
