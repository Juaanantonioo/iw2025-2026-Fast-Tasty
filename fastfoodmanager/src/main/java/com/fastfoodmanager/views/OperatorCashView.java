package com.fastfoodmanager.views;

import com.fastfoodmanager.domain.CashClosure;
import com.fastfoodmanager.service.CashService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
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
    private final Div closuresBox = new Div();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public OperatorCashView(CashService cashService) {
        this.cashService = cashService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("dashboard-bg");

        Div page = new Div();
        page.addClassName("ft-page");

        Div header = new Div();
        header.addClassName("ft-topbar");

        Div headerBlock = new Div();
        H1 title = new H1("Caja (hoy)");
        title.addClassName("ft-title");
        Paragraph subtitle = new Paragraph("Resumen del periodo desde el último cierre y cierres realizados hoy.");
        subtitle.addClassName("ft-subtitle");
        headerBlock.add(title, subtitle);

        Div actions = new Div();
        actions.addClassName("ft-actions");

        Button refresh = new Button("Refrescar", e -> reload());
        refresh.getElement().getThemeList().add("primary");

        Button closeCash = new Button("Cerrar caja", e -> doCloseCash());
        closeCash.getElement().getThemeList().add("error");

        actions.add(refresh, closeCash);
        header.add(headerBlock, actions);

        Div currentCard = new Div();
        currentCard.addClassName("ft-card");

        H3 currentTitle = new H3("Facturado desde el último cierre");
        currentTitle.getStyle().set("margin", "0");

        amount.getStyle().set("font-size", "2.6rem").set("font-weight", "900");
        period.getStyle().set("color", "var(--ft-muted)");

        currentCard.add(currentTitle, amount, new Div(period));

        Div closuresCard = new Div();
        closuresCard.addClassName("ft-card");

        H3 closuresTitle = new H3("Cierres de hoy");
        closuresTitle.getStyle().set("margin", "0");

        closuresBox.getStyle().set("margin-top", "10px");
        closuresCard.add(closuresTitle, closuresBox);

        page.add(header, currentCard, closuresCard);
        add(page);

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
            empty.getStyle().set("color", "var(--ft-muted)");
            closuresBox.add(empty);
            return;
        }

        for (CashClosure c : closures) {
            Div row = new Div();
            row.getStyle()
                    .set("padding", "10px 0")
                    .set("border-bottom", "1px solid rgba(0,0,0,0.06)");

            Span text = new Span(
                    c.getClosedAt().format(dtf) + " — " + formatMoney(c.getAmount()) + " — (" + c.getOperatorUsername() + ")"
            );
            row.add(text);
            closuresBox.add(row);
        }
    }

    private String formatMoney(double v) {
        return String.format("€ %.2f", v);
    }
}