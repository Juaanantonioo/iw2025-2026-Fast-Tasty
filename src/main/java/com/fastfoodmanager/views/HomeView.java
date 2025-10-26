package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.MenuService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class HomeView extends VerticalLayout {
    private final MenuService menuService;
    private final Grid<Product> grid = new Grid<>(Product.class, false);

    public HomeView(MenuService menuService) {
        this.menuService = menuService;

        add(new H1("Welcome to Fast&Tasty"));

        grid.addColumn(Product::getName).setHeader("Producto").setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Descripción").setAutoWidth(true);
        grid.addColumn(p -> p.getPrice() + " €").setHeader("Precio").setAutoWidth(true);
        grid.setAllRowsVisible(true);

        grid.setItems(menuService.findActiveProducts());
        add(grid);
    }
}
