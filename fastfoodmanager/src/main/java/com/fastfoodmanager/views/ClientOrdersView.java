package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.service.OrderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        grid.addComponentColumn(o -> {
            Button modificar = new Button("Modificar");
            Button cancelar = new Button("Cancelar");

            boolean editable = o != null
                    && o.getStatus() != null
                    && "ENVIADO".equalsIgnoreCase(o.getStatus())
                    && !"CANCELADO".equalsIgnoreCase(o.getStatus());

            modificar.setEnabled(editable);
            cancelar.setEnabled(editable);

            modificar.addClickListener(e -> openModifyDialog(o));
            cancelar.addClickListener(e -> confirmCancel(o));

            HorizontalLayout hl = new HorizontalLayout(modificar, cancelar);
            hl.setSpacing(true);
            return hl;
        }).setHeader("Acciones").setAutoWidth(true);

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

    private void confirmCancel(Order o) {
        if (o == null) return;

        ConfirmDialog cd = new ConfirmDialog();
        cd.setHeader("Cancelar pedido #" + o.getId());
        cd.setText("Solo puedes cancelar si está ENVIADO y ya pagado. Si cancelas, se simulará la devolución del pago.");
        cd.setCancelable(true);
        cd.setConfirmText("Cancelar pedido");

        cd.addConfirmListener(ev -> {
            try {
                orderService.cancelPaidOrderBeforeKitchen(o.getId());
                Notification.show("Pedido cancelado");
                refresh();
            } catch (Exception ex) {
                Notification.show(ex.getMessage() != null ? ex.getMessage() : "No se pudo cancelar");
            }
        });

        cd.open();
    }

    // Diálogo para modificar cantidades de un pedido pagado antes de cocina
    private void openModifyDialog(Order o) {
        if (o == null) return;

        Order fresh;
        try {
            fresh = orderService.findWithItemsOrThrow(o.getId());
        } catch (Exception ex) {
            Notification.show(ex.getMessage() != null ? ex.getMessage() : "No se pudo cargar el pedido");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modificar pedido #" + o.getId());

        FormLayout form = new FormLayout();
        form.setWidth("600px");

        Map<Long, IntegerField> fields = new LinkedHashMap<>();

        if (fresh.getItems() != null) {
            for (OrderItem item : fresh.getItems()) {
                if (item.getProduct() == null || item.getProduct().getId() == null) continue;

                Long pid = item.getProduct().getId();
                String label = (item.getProduct().getName() != null ? item.getProduct().getName() : "Producto");

                IntegerField qty = new IntegerField(label);
                qty.setMin(0);
                qty.setStepButtonsVisible(true);
                qty.setValue(item.getQuantity());

                fields.put(pid, qty);
                form.add(qty);
            }
        }

        Button guardar = new Button("Guardar cambios");
        Button cerrar = new Button("Cerrar", ev -> dialog.close());

        guardar.addClickListener(ev -> {
            try {
                Map<Long, Integer> map = new LinkedHashMap<>();
                for (Map.Entry<Long, IntegerField> en : fields.entrySet()) {
                    Integer v = en.getValue().getValue();
                    map.put(en.getKey(), v == null ? 0 : v);
                }

                orderService.updatePaidOrderItemsBeforeKitchen(o.getId(), map);
                Notification.show("Pedido actualizado");
                dialog.close();
                refresh();
            } catch (Exception ex) {
                Notification.show(ex.getMessage() != null ? ex.getMessage() : "No se pudo modificar");
            }
        });

        HorizontalLayout actions = new HorizontalLayout(guardar, cerrar);
        dialog.add(form, actions);
        dialog.open();
    }

}