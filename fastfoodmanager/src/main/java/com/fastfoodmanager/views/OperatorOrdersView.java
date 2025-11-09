package com.fastfoodmanager.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Pedidos")
@RolesAllowed({"OPERATOR","ADMIN"})
@Route(value = "operator/orders", layout = MainLayout.class)
public class OperatorOrdersView extends VerticalLayout {

    private final Grid<Order> grid = new Grid<>(Order.class, false);
    private final List<Order> orders = new ArrayList<>();

    public OperatorOrdersView() {
        add(new H1("📦 Pedidos asignados"));

        grid.addColumn(Order::getId).setHeader("ID Pedido");
        grid.addColumn(Order::getCliente).setHeader("Cliente");
        grid.addColumn(Order::getEstado).setHeader("Estado");

        grid.addComponentColumn(order -> {
            Button siguiente = new Button("Cambiar estado", e -> avanzarEstado(order));
            return siguiente;
        }).setHeader("Acción");

        grid.setItems(orders);
        add(grid);

        cargarPedidos();
    }

    private void cargarPedidos() {
        // 🔧 Temporal: en el futuro se traerán de la base de datos
        orders.add(new Order(1L, "Carlos", "Enviado a cocina"));
        orders.add(new Order(2L, "Lucía", "Preparando"));
        orders.add(new Order(3L, "Marta", "Listo"));
        grid.getDataProvider().refreshAll();
    }

    private void avanzarEstado(Order o) {
        String nuevoEstado;
        switch (o.getEstado()) {
            case "Enviado a cocina" -> nuevoEstado = "Preparando";
            case "Preparando" -> nuevoEstado = "Listo";
            case "Listo" -> nuevoEstado = "En reparto";
            default -> nuevoEstado = "Completado";
        }
        o.setEstado(nuevoEstado);
        grid.getDataProvider().refreshAll();
        Notification.show("Pedido " + o.getId() + " → " + nuevoEstado);
    }

    // Clase interna temporal (simula pedidos)
    public static class Order {
        private Long id;
        private String cliente;
        private String estado;

        public Order(Long id, String cliente, String estado) {
            this.id = id;
            this.cliente = cliente;
            this.estado = estado;
        }

        public Long getId() { return id; }
        public String getCliente() { return cliente; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}
