package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.service.OrderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Mis pedidos")
@RolesAllowed("USER")
@Route(value = "client/orders", layout = MainLayout.class)
public class ClientOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ClientOrdersView(OrderService orderService) {
        this.orderService = orderService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("Mis pedidos");
        Button refreshBtn = new Button("Refrescar", e -> refresh());

        UI.getCurrent().setPollInterval(5000);
        UI.getCurrent().addPollListener(e -> refresh());

        HorizontalLayout top = new HorizontalLayout(title, refreshBtn);
        top.setWidthFull();
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);
        top.setAlignItems(Alignment.CENTER);

        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCreatedAt() == null ? "-" : o.getCreatedAt().format(fmt))
                .setHeader("Fecha").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getStatus() == null ? "-" : o.getStatus())
                .setHeader("Estado").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> formatItemsSafe(o.getItems()))
                .setHeader("Productos").setFlexGrow(1);
        grid.addColumn(o -> String.format("€ %.2f", o.getTotal() == null ? 0.0 : o.getTotal()))
                .setHeader("Total").setAutoWidth(true);

        add(top, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        List<Order> data = orderService.findForCurrentCustomerWithItems();
        grid.setItems(data);
        grid.getDataProvider().refreshAll();
    }

    private String formatItemsSafe(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "-";
        return items.stream()
                .map(i -> (i.getProduct() != null ? i.getProduct().getName() : "Producto") + " x" + i.getQuantity())
                .reduce((a, b) -> a + ", " + b)
                .orElse("-");
    }
}
