package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.ProductService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("products")
public class ProductView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> grid = new Grid<>(Product.class);

    public ProductView(@Autowired ProductService productService) {
        this.productService = productService;

        TextField name = new TextField("Name");
        TextField description = new TextField("Description");
        TextField price = new TextField("Price");

        Button addButton = new Button("Add Product", e -> {
            Product product = new Product(
                    name.getValue(),
                    description.getValue(),
                    Double.parseDouble(price.getValue()),
                    true
            );
            productService.save(product);
            updateList();
            name.clear();
            description.clear();
            price.clear();
        });

        add(name, description, price, addButton, grid);
        updateList();
    }

    private void updateList() {
        grid.setItems(productService.findAll());
    }
}
