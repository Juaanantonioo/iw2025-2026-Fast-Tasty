package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.service.OrderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Mis pedidos")
@RolesAllowed({"USER","ADMIN"})
@Route(value = "mis-pedidos", layout = MainLayout.class)
public class MyOrdersView extends VerticalLayout {

    private final OrderService orderService;
    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MyOrdersView(OrderService orderService) {
        this.orderService = orderService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        var title = new H2("Mis pedidos");
        var refreshBtn = new Button("Refrescar", e -> refresh());

        // 🔄 Auto-refresco cada 4 segundos
        UI.getCurrent().setPollInterval(4000);
        UI.getCurrent().addPollListener(e -> refresh());

        var top = new HorizontalLayout(title, refreshBtn);
        top.setWidthFull();
        top.setAlignItems(Alignment.CENTER);
        top.setJustifyContentMode(JustifyContentMode.BETWEEN);

        grid.addColumn(Order::getId).setHeader("ID").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getCreatedAt() == null ? "-" : o.getCreatedAt().format(fmt))
                .setHeader("Fecha").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getStatus() == null ? "-" : o.getStatus())
                .setHeader("Estado").setAutoWidth(true);
        grid.addColumn(o -> String.format("€ %.2f", o.getTotal() == null ? 0.0 : o.getTotal()))
                .setHeader("Total").setAutoWidth(true).setSortable(true);

        add(top, grid);
        setFlexGrow(1, grid);

        refresh();
    }

    private void refresh() {
        List<Order> mine = orderService.findForCurrentCustomer();
        grid.setItems(mine);
    }
}
