package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@PageTitle("Stock | Operario")
@RolesAllowed({"OPERATOR", "ADMIN"})
@Route(value = "operator/stock", layout = MainLayout.class)
public class OperatorStockView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> grid = new Grid<>(Product.class, false);

    public OperatorStockView(ProductService productService) {
        this.productService = productService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("dashboard-bg");

        Div page = new Div();
        page.addClassName("ft-page");

        Div header = new Div();
        header.addClassName("ft-topbar");

        Div headerBlock = new Div();
        H1 title = new H1("Gestión de Stock");
        title.addClassName("ft-title");
        Paragraph subtitle = new Paragraph("Edita el stock disponible de cada producto.");
        subtitle.addClassName("ft-subtitle");
        headerBlock.add(title, subtitle);

        Div actions = new Div();
        actions.addClassName("ft-actions");
        Button refresh = new Button("Refrescar", e -> refresh());
        refresh.getElement().getThemeList().add("primary");
        actions.add(refresh);

        header.add(headerBlock, actions);

        Div card = new Div();
        card.addClassName("ft-card");

        grid.setWidthFull();

        grid.addColumn(Product::getName).setHeader("Producto").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Product::getDescription).setHeader("Descripción").setAutoWidth(true).setFlexGrow(2);
        grid.addColumn(Product::getPrice).setHeader("Precio (€)").setAutoWidth(true).setFlexGrow(0);

        Grid.Column<Product> stockCol = grid.addColumn(Product::getStock)
                .setHeader("Stock").setAutoWidth(true).setFlexGrow(0);

        Editor<Product> editor = grid.getEditor();
        Binder<Product> binder = new Binder<>(Product.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        IntegerField stockField = new IntegerField();
        stockField.setMin(0);
        stockField.setStep(1);
        stockField.setStepButtonsVisible(true);

        binder.forField(stockField)
                .withValidator(v -> v == null || v >= 0, "El stock no puede ser negativo")
                .bind(Product::getStock, Product::setStock);

        stockCol.setEditorComponent(stockField);

        grid.addComponentColumn(p -> {
            Button edit = new Button("Editar", e -> {
                editor.editItem(p);
                stockField.focus();
            });
            edit.getElement().getThemeList().add("tertiary");

            Button save = new Button("Guardar", e -> {
                try {
                    editor.save();
                    Integer newVal = stockField.getValue() == null ? 0 : stockField.getValue();
                    productService.updateStock(p.getId(), newVal);
                    Notification.show("Stock actualizado", 2000, Notification.Position.MIDDLE);
                    editor.cancel();
                    refresh();
                } catch (Exception ex) {
                    Notification.show("Error guardando: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                }
            });
            save.getElement().getThemeList().add("primary");

            Button cancel = new Button("Cancelar", e -> {
                editor.cancel();
                refresh();
            });
            cancel.getElement().getThemeList().add("tertiary-inline");

            HorizontalLayout hl = new HorizontalLayout(edit, save, cancel);
            hl.setSpacing(true);
            hl.setWrap(true);                    // CLAVE anti-overflow
            hl.addClassName("grid-actions");     // CSS ayuda si es necesario
            hl.getStyle().set("max-width", "100%");
            return hl;
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        card.add(grid);

        page.add(header, card);
        add(page);

        refresh();
    }

    private void refresh() {
        List<Product> products = productService.findAll();
        grid.setItems(products);
        grid.getDataProvider().refreshAll();
    }
}