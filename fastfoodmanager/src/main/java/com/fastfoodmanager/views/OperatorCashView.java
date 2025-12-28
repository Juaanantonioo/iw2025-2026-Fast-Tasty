package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.CashClosure;
import com.fastfoodmanager.service.CashService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Caja | Operario")
@Route(value = "operator/caja", layout = MainLayout.class)
@RolesAllowed("OPERATOR")
public class OperatorCashView extends VerticalLayout {

    private final CashService cashService;

    private final Span amount = new Span();
    private final Span period = new Span();
    private final VerticalLayout closuresBox = new VerticalLayout();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OperatorCashView(CashService cashService) {
        this.cashService = cashService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Caja (hoy)");
        title.getStyle().set("margin", "0");

        Button refresh = new Button("Refrescar", e -> reload());
        Button closeCash = new Button("Cerrar caja", e -> doCloseCash());

        HorizontalLayout actions = new HorizontalLayout(refresh, closeCash);
        actions.setSpacing(true);

        H3 current = new H3("Facturado desde el último cierre:");
        current.getStyle().set("margin", "0.5rem 0 0 0");

        amount.getStyle()
                .set("font-size", "2rem")
                .set("font-weight", "800");

        period.getStyle().set("opacity", "0.8");

        closuresBox.setPadding(false);
        closuresBox.setSpacing(false);

        add(title, actions, current, amount, period, new H3("Cierres de hoy"), closuresBox);

        reload();
    }

    private void doCloseCash() {
        String operator = CashUiUtil.currentUsernameOr("operario");
        CashClosure cc = cashService.closeCash(operator);
        Notification.show("Caja cerrada: " + formatMoney(cc.getAmount()), 2500, Notification.Position.TOP_CENTER);
        reload();
    }

    private void reload() {
        CashService.CashSnapshot snap = cashService.getTodaySnapshot();

        amount.setText(formatMoney(snap.currentAmount()));
        period.setText("Periodo: " + snap.periodStart().format(dtf) + " → " + snap.periodEnd().format(dtf));

        renderClosures(snap.closuresToday());
    }

    private void renderClosures(List<CashClosure> closures) {
        closuresBox.removeAll();

        if (closures == null || closures.isEmpty()) {
            Span empty = new Span("Aún no hay cierres hoy.");
            empty.getStyle().set("opacity", "0.7");
            closuresBox.add(empty);
            return;
        }

        for (CashClosure c : closures) {
            Span row = new Span(
                    c.getClosedAt().format(dtf) + " — " + formatMoney(c.getAmount()) + " — (" + c.getOperatorUsername() + ")"
            );
            row.getStyle()
                    .set("padding", "0.25rem 0")
                    .set("border-bottom", "1px solid rgba(0,0,0,0.06)");
            closuresBox.add(row);
        }
    }

    private String formatMoney(double v) {
        return String.format("€ %.2f", v);
    }
}
