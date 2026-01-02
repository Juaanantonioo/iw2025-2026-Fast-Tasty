package com.fastfoodmanager.views;

import com.fastfoodmanager.service.CashService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

@PageTitle("Estadísticas | Operario")
@Route(value = "operator/estadisticas", layout = MainLayout.class)
@RolesAllowed("OPERATOR")
public class OperatorStatsView extends VerticalLayout {

    private enum Mode { SEMANA, MES, ANO }

    private final CashService cashService;

    private final Select<Mode> mode = new Select<>();
    private final DatePicker baseDate = new DatePicker();
    private final Button apply = new Button("Aplicar");

    private final Span rangeLabel = new Span();
    private final Div chart = new Div();
    private final Div listBox = new Div();

    public OperatorStatsView(CashService cashService) {
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
        H1 title = new H1("Estadísticas de caja");
        title.addClassName("ft-title");
        Paragraph subtitle = new Paragraph("Analiza cierres por semana, mes o año.");
        subtitle.addClassName("ft-subtitle");
        headerBlock.add(title, subtitle);

        header.add(headerBlock);

        Div filtersCard = new Div();
        filtersCard.addClassName("ft-card");

        mode.setLabel("Filtro");
        mode.setItems(Mode.SEMANA, Mode.MES, Mode.ANO);
        mode.setValue(Mode.SEMANA);

        baseDate.setLabel("Fecha base");
        baseDate.setValue(LocalDate.now());

        apply.getElement().getThemeList().add("primary");
        apply.addClickListener(e -> reload());

        HorizontalLayout filters = new HorizontalLayout(mode, baseDate, apply);
        filters.setDefaultVerticalComponentAlignment(Alignment.END);
        filters.setSpacing(true);

        rangeLabel.getStyle().set("color", "var(--ft-muted)");

        filtersCard.add(filters, rangeLabel);

        Div results = new Div();
        results.addClassName("ft-grid-2");

        Div chartCard = new Div();
        chartCard.addClassName("ft-card");
        chart.getStyle().set("width", "100%");
        chartCard.add(new H3("Resumen"), chart);

        Div listCard = new Div();
        listCard.addClassName("ft-card");
        listBox.getStyle().set("width", "100%");
        listCard.add(new H3("Detalle"), listBox);

        results.add(chartCard, listCard);

        page.add(header, filtersCard, results);
        add(page);

        reload();
    }

    private void reload() {
        LocalDate base = baseDate.getValue();
        if (base == null) {
            Notification.show("Elige una fecha base", 2000, Notification.Position.TOP_CENTER);
            return;
        }

        Mode m = mode.getValue() == null ? Mode.SEMANA : mode.getValue();
        switch (m) {
            case SEMANA -> renderWeek(base);
            case MES -> renderMonth(base);
            case ANO -> renderYear(base);
        }
    }

    private void renderWeek(LocalDate base) {
        LocalDate monday = base.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        rangeLabel.setText("Mostrando SEMANA: " + monday + " → " + sunday);

        LocalDateTime from = monday.atStartOfDay();
        LocalDateTime toExclusive = sunday.plusDays(1).atStartOfDay();

        Map<DayOfWeek, Double> map = cashService.getClosedTotalsByDayOfWeek(from, toExclusive);

        List<DayOfWeek> order = List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        );

        LinkedHashMap<String, Double> series = new LinkedHashMap<>();
        for (DayOfWeek d : order) {
            String label = capitalize(d.getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
            series.put(label, map.getOrDefault(d, 0.0));
        }

        renderBarChart(series);
        renderList(series);
    }

    private void renderMonth(LocalDate base) {
        YearMonth ym = YearMonth.from(base);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        rangeLabel.setText("Mostrando MES: " + from + " → " + to);

        Map<LocalDate, Double> totals = cashService.getClosedTotalsByDay(from, to);

        LinkedHashMap<String, Double> series = new LinkedHashMap<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            series.put(String.valueOf(d.getDayOfMonth()), totals.getOrDefault(d, 0.0));
        }

        renderBarChart(series);
        renderList(series);
    }

    private void renderYear(LocalDate base) {
        int year = base.getYear();
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        rangeLabel.setText("Mostrando AÑO: " + year);

        Map<YearMonth, Double> totals = cashService.getClosedTotalsByMonth(from, to);

        LinkedHashMap<String, Double> series = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            String label = capitalize(ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES")).replace(".", ""));
            series.put(label, totals.getOrDefault(ym, 0.0));
        }

        renderBarChart(series);
        renderList(series);
    }

    private void renderBarChart(LinkedHashMap<String, Double> series) {
        chart.removeAll();

        double max = series.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (max <= 0) max = 1.0;

        Div barWrap = new Div();
        barWrap.getStyle()
                .set("display", "flex")
                .set("gap", "10px")
                .set("align-items", "flex-end")
                .set("overflow-x", "auto")
                .set("padding", "10px 2px");

        for (var e : series.entrySet()) {
            String label = e.getKey();
            double value = e.getValue();
            double pct = value / max;

            Div col = new Div();
            col.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("min-width", "44px");

            Span v = new Span(String.format("€%.2f", value));
            v.getStyle().set("font-size", "12px").set("color", "var(--ft-muted)");

            Div bar = new Div();
            bar.getStyle()
                    .set("width", "36px")
                    .set("height", Math.round(220 * pct) + "px")
                    .set("border-radius", "10px")
                    .set("background", "var(--ft-primary)");

            Span l = new Span(label);
            l.getStyle().set("font-size", "12px").set("color", "var(--ft-muted)");

            col.add(v, bar, l);
            barWrap.add(col);
        }

        chart.add(barWrap);
    }

    private void renderList(LinkedHashMap<String, Double> series) {
        listBox.removeAll();

        for (var e : series.entrySet()) {
            Div row = new Div();
            row.getStyle()
                    .set("display", "flex")
                    .set("justify-content", "space-between")
                    .set("padding", "10px 0")
                    .set("border-bottom", "1px solid rgba(0,0,0,0.06)");

            Span left = new Span(e.getKey());
            Span right = new Span(String.format("€ %.2f", e.getValue()));
            right.getStyle().set("font-weight", "800");

            row.add(left, right);
            listBox.add(row);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}