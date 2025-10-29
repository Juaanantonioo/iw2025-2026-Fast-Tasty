package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.service.CartService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Carrito | FastTasty")
@Route(value = "carrito", layout = MainLayout.class)
//@CssImport("./themes/my-theme/carrito.css")

public class CarritoView extends VerticalLayout {

    private final CartService cartService;

    public CarritoView(CartService cartService) {
        this.cartService = cartService;

        addClassName("carrito-view");
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        createHeader();
        createContent();
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("carrito-header");
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        Button backButton = new Button("Volver a la Carta", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addClassName("back-button");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("carta")));

        H1 title = new H1("Tu Carrito de Compra 🛒");
        title.addClassName("carrito-title");

        header.add(backButton, title);
        header.expand(title);

        add(header);
    }

    private void createContent() {
        List<Product> cartItems = cartService.getCartItems();

        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            showCartItems(cartItems);
        }
    }

    private void showEmptyCart() {
        Div emptyState = new Div();
        emptyState.addClassName("empty-state");

        Icon cartIcon = VaadinIcon.CART.create();
        cartIcon.addClassName("empty-cart-icon");

        H2 emptyTitle = new H2("Tu carrito está vacío");
        Paragraph emptyMessage = new Paragraph("¡Descubre nuestros deliciosos platos y añade algunos a tu pedido!");

        Button browseButton = new Button("Ver la Carta", e ->
                getUI().ifPresent(ui -> ui.navigate("carta")));
        browseButton.addClassName("browse-button");

        emptyState.add(cartIcon, emptyTitle, emptyMessage, browseButton);
        add(emptyState);
    }

    private void showCartItems(List<Product> cartItems) {
        Map<Product, Long> productCounts = cartItems.stream()
                .collect(Collectors.groupingBy(product -> product, Collectors.counting()));

        VerticalLayout itemsLayout = new VerticalLayout();
        itemsLayout.addClassName("items-layout");
        itemsLayout.setPadding(false);
        itemsLayout.setSpacing(true);

        double total = 0.0;

        for (Map.Entry<Product, Long> entry : productCounts.entrySet()) {
            Product product = entry.getKey();
            Long quantity = entry.getValue();
            double subtotal = product.getPrice() * quantity;
            total += subtotal;

            HorizontalLayout itemLayout = createCartItemLayout(product, quantity, subtotal);
            itemsLayout.add(itemLayout);
        }

        Div summary = createOrderSummary(total);
        HorizontalLayout actionButtons = createActionButtons();

        add(itemsLayout, summary, actionButtons);
    }

    private HorizontalLayout createCartItemLayout(Product product, Long quantity, double subtotal) {
        HorizontalLayout itemLayout = new HorizontalLayout();
        itemLayout.addClassName("cart-item");
        itemLayout.setWidthFull();
        itemLayout.setAlignItems(Alignment.CENTER);

        Div productInfo = new Div();
        productInfo.addClassName("product-info");

        H2 productName = new H2(product.getName());
        productName.addClassName("product-name");

        Paragraph productDesc = new Paragraph(product.getDescription());
        productDesc.addClassName("product-desc");

        productInfo.add(productName, productDesc);

        Div quantityInfo = new Div();
        quantityInfo.addClassName("quantity-info");
        quantityInfo.setText("Cantidad: " + quantity);

        Div priceInfo = new Div();
        priceInfo.addClassName("price-info");
        priceInfo.setText(String.format("%.2f €", subtotal));

        // Botones para modificar cantidad
        Button removeButton = new Button("-", e -> {
            cartService.removeProduct(product);
            getUI().ifPresent(ui -> ui.navigate("carrito")); // Recargar la vista
        });
        removeButton.addClassName("cart-remove-button");

        Button addButton = new Button("+", e -> {
            cartService.addProduct(product);
            getUI().ifPresent(ui -> ui.navigate("carrito")); // Recargar la vista
        });
        addButton.addClassName("cart-add-button");

        HorizontalLayout buttons = new HorizontalLayout(removeButton, addButton);
        buttons.setSpacing(true);

        itemLayout.add(productInfo, quantityInfo, priceInfo, buttons);
        itemLayout.expand(productInfo);

        return itemLayout;
    }

    private Div createOrderSummary(double total) {
        Div summary = new Div();
        summary.addClassName("order-summary");

        H2 summaryTitle = new H2("Resumen del Pedido");
        Paragraph totalText = new Paragraph(String.format("Total: %.2f €", total));
        totalText.addClassName("total-price");

        summary.add(summaryTitle, totalText);
        return summary;
    }

    private HorizontalLayout createActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addClassName("action-buttons");

        Button continueShopping = new Button("Seguir Comprando", e ->
                getUI().ifPresent(ui -> ui.navigate("carta")));
        continueShopping.addClassName("continue-button");

        Button clearCart = new Button("Vaciar Carrito", e -> {
            cartService.clearCart();
            Notification.show("Carrito vaciado", 2000, Notification.Position.BOTTOM_END);
            getUI().ifPresent(ui -> ui.navigate("carrito"));
        });
        clearCart.addClassName("clear-button");

        Button placeOrder = new Button("Realizar Pedido", e -> {
            if (cartService.getItemCount() > 0) {
                Notification.show("¡Pedido realizado con éxito!", 3000, Notification.Position.MIDDLE);
                cartService.clearCart();
                getUI().ifPresent(ui -> ui.navigate("carta"));
            } else {
                Notification.show("El carrito está vacío", 2000, Notification.Position.BOTTOM_END);
            }
        });
        placeOrder.addClassName("order-button");

        buttons.add(continueShopping, clearCart, placeOrder);
        return buttons;
    }
}