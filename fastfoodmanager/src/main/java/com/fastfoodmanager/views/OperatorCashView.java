package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.CashClosure;
import com.fastfoodmanager.service.CashService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Caja | Operario")
@RolesAllowed({"OPERATOR","ADMIN"})
@Route(value = "operator/caja", layout = MainLayout.class)
public class OperatorCashView extends VerticalLayout {

    private final CashService cashService;

    private final Span cajaLabel = new Span();
    private final Span periodoLabel = new Span();

    private final Grid<CashClosure> grid = new Grid<>(CashClosure.class, false);
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OperatorCashView(CashService cashService) {
        this.cashService = cashService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Caja");

        Button actualizar = new Button("Actualizar", e -> refresh());

        Button cerrarCaja = new Button("Refrescar (Cerrar caja)", e -> {
            try {
                String op = getCurrentUsername();
                CashClosure cc = cashService.closeCash(op);
                Notification.show("Caja cerrada: € " + String.format("%.2f", cc.getAmount()), 2500,
                        Notification.Position.MIDDLE);
                refresh();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3500, Notification.Position.MIDDLE);
            }
        });

        HorizontalLayout actions = new HorizontalLayout(actualizar, cerrarCaja);
        actions.setSpacing(true);

        cajaLabel.getStyle().set("font-size", "1.2rem").set("font-weight", "600");
        periodoLabel.getStyle().set("color", "#666");

        grid.addColumn(CashClosure::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(c -> c.getClosedAt() == null ? "-" : c.getClosedAt().format(fmt))
                .setHeader("Cerrado").setAutoWidth(true);
        grid.addColumn(c -> c.getOperatorUsername() == null ? "-" : c.getOperatorUsername())
                .setHeader("Operario").setAutoWidth(true);
        grid.addColumn(c -> c.getPeriodStart() == null ? "-" : c.getPeriodStart().format(fmt))
                .setHeader("Desde").setAutoWidth(true);
        grid.addColumn(c -> c.getPeriodEnd() == null ? "-" : c.getPeriodEnd().format(fmt))
                .setHeader("Hasta").setAutoWidth(true);
        grid.addColumn(c -> "€ " + String.format("%.2f", c.getAmount() == null ? 0.0 : c.getAmount()))
                .setHeader("Importe").setAutoWidth(true);

        add(title, actions, cajaLabel, periodoLabel, grid);
        setFlexGrow(1, grid);

        // Autorefresco opcional
        UI.getCurrent().setPollInterval(10000);
        UI.getCurrent().addPollListener(e -> refresh());

        refresh();
    }

    private void refresh() {
        var snap = cashService.getTodaySnapshot();

        cajaLabel.setText("Caja actual: € " + String.format("%.2f", snap.currentAmount()));
        periodoLabel.setText("Periodo: " + snap.periodStart().format(fmt) + " → " + snap.periodEnd().format(fmt));

        List<CashClosure> data = snap.closuresToday();
        grid.setItems(data);
        grid.getDataProvider().refreshAll();
    }

    private String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        String name = a.getName();
        return "anonymousUser".equals(name) ? null : name;
    }
}
