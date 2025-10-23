package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.MenuService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("")
public class HomeView extends VerticalLayout {
    private final MenuService menuService;
    private final Grid<Product> grid = new Grid<>(Product.class, false);

    public HomeView(MenuService menuService) {
        this.menuService = menuService;

        add(new H1("Welcome to Fast Food Manager"));
        add(new RouterLink("Ir a gestión de productos", ProductView.class)); // opcional

        grid.addColumn(Product::getName).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Descripción").setAutoWidth(true);
        grid.addColumn(p -> p.getPrice() + " €").setHeader("Precio").setAutoWidth(true);
        grid.setAllRowsVisible(true);

        loadData();
        add(grid);
    }

    private void loadData() {
        grid.setItems(menuService.findActiveProducts());
    }
}
