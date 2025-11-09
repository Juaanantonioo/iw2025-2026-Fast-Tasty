package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@PageTitle("Stock")
@RolesAllowed({"OPERATOR", "ADMIN"})
@Route(value = "operator/stock", layout = MainLayout.class)
public class OperatorStockView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> grid = new Grid<>(Product.class, false);

    public OperatorStockView(ProductService productService) {
        this.productService = productService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Gestión de Stock"));

        // Columnas visibles
        grid.addColumn(Product::getName)
                .setHeader("Producto").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(Product::getDescription)
                .setHeader("Descripción").setAutoWidth(true).setFlexGrow(2);
        grid.addColumn(Product::getPrice)
                .setHeader("Precio (€)").setAutoWidth(true).setFlexGrow(0);

        Grid.Column<Product> stockCol = grid.addColumn(Product::getStock)
                .setHeader("Stock").setAutoWidth(true).setFlexGrow(0);

        // Editor inline para Stock
        Editor<Product> editor = grid.getEditor();
        Binder<Product> binder = new Binder<>(Product.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        IntegerField stockField = new IntegerField();
        stockField.setMin(0);
        stockField.setStep(1); // reemplazo de setHasControls()
        stockField.setStepButtonsVisible(true);

        binder.forField(stockField)
                .withValidator(v -> v == null || v >= 0, "El stock no puede ser negativo")
                .bind(Product::getStock, Product::setStock);

        stockCol.setEditorComponent(stockField);

        // Columna de acciones
        grid.addComponentColumn(p -> {
            Button edit = new Button("Editar", e -> {
                editor.editItem(p);
                stockField.focus();
            });

            Button save = new Button("Guardar", e -> {
                try {
                    editor.save();
                    Integer newVal = stockField.getValue() == null ? 0 : stockField.getValue();
                    productService.updateStock(p.getId(), newVal);
                    Notification.show("Stock actualizado", 2000, Notification.Position.MIDDLE);
                    editor.cancel();
                    refresh();
                } catch (Exception ex) {
                    Notification.show("Error guardando: " + ex.getMessage(),
                            3000, Notification.Position.MIDDLE);
                }
            });

            Button cancel = new Button("Cancelar", e -> {
                editor.cancel();
                refresh();
            });

            HorizontalLayout actions = new HorizontalLayout(edit, save, cancel);
            actions.setSpacing(true);
            return actions;
        }).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        add(grid);
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        List<Product> products = productService.findAll();
        grid.setItems(products);
        grid.getDataProvider().refreshAll();
    }
}
