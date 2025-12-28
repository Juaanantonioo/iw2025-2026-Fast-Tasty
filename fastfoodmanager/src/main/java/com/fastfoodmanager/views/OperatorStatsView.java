package com.fastfoodmanager.views;

import com.fastfoodmanager.service.CashService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

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
    private final VerticalLayout list = new VerticalLayout();

    private final DateTimeFormatter dmy = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OperatorStatsView(CashService cashService) {
        this.cashService = cashService;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Estadísticas de caja");
        title.getStyle().set("margin", "0");

        mode.setLabel("Filtro");
        mode.setItems(Mode.SEMANA, Mode.MES, Mode.ANO);
        mode.setValue(Mode.SEMANA);

        baseDate.setLabel("Fecha base");
        baseDate.setValue(LocalDate.now());

        apply.addClickListener(e -> reload());

        HorizontalLayout filters = new HorizontalLayout(mode, baseDate, apply);
        filters.setDefaultVerticalComponentAlignment(Alignment.END);
        filters.setSpacing(true);

        rangeLabel.getStyle().set("opacity", "0.75");

        chart.getStyle()
                .set("margin-top", "1rem")
                .set("padding", "1rem")
                .set("border-radius", "12px")
                .set("background", "rgba(0,0,0,0.03)");

        list.setPadding(false);
        list.setSpacing(false);

        add(title, filters, rangeLabel, chart, list);

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

        // Orden L..D
        List<DayOfWeek> order = List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        );

        LinkedHashMap<String, Double> series = new LinkedHashMap<>();
        for (DayOfWeek d : order) {
            String label = d.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            label = capitalize(label);
            series.put(label, map.getOrDefault(d, 0.0));
        }

        renderBarChart(series, 7);
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
            String label = String.valueOf(d.getDayOfMonth());
            series.put(label, totals.getOrDefault(d, 0.0));
        }

        renderBarChart(series, series.size());
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
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            label = capitalize(label.replace(".", ""));
            series.put(label, totals.getOrDefault(ym, 0.0));
        }

        renderBarChart(series, 12);
        renderList(series);
    }

    /** Gráfico de barras simple: sin dependencias externas. */
    private void renderBarChart(LinkedHashMap<String, Double> series, int n) {
        chart.removeAll();

        double max = series.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        if (max <= 0) max = 1.0;

        // contenedor barras
        HorizontalLayout bars = new HorizontalLayout();
        bars.setWidthFull();
        bars.setSpacing(true);
        bars.setDefaultVerticalComponentAlignment(Alignment.END);

        // Ajuste de ancho según cantidad (mes = muchas barras)
        String barWidth = (n <= 12) ? "42px" : (n <= 31 ? "18px" : "12px");

        for (Map.Entry<String, Double> e : series.entrySet()) {
            String label = e.getKey();
            double value = e.getValue();

            Div bar = new Div();
            double pct = value / max;
            int height = (int) Math.round(220 * pct); // 0..220 px

            bar.getStyle()
                    .set("width", barWidth)
                    .set("height", height + "px")
                    .set("border-radius", "8px")
                    .set("background", "#2b6cb0"); // azul
            // Si prefieres sin color fijo, dímelo y lo hago neutro

            Span v = new Span(String.format("€%.2f", value));
            v.getStyle().set("font-size", "0.75rem").set("opacity", "0.85");

            Span l = new Span(label);
            l.getStyle().set("font-size", "0.75rem").set("opacity", "0.85");

            VerticalLayout col = new VerticalLayout(v, bar, l);
            col.setPadding(false);
            col.setSpacing(false);
            col.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            col.getStyle().set("min-width", barWidth);

            bars.add(col);
        }

        chart.add(bars);
    }

    private void renderList(LinkedHashMap<String, Double> series) {
        list.removeAll();
        list.getStyle().set("margin-top", "1rem");

        Div box = new Div();
        box.getStyle()
                .set("padding", "1rem")
                .set("border-radius", "12px")
                .set("border", "1px solid rgba(0,0,0,0.08)")
                .set("background", "white")
                .set("width", "320px");

        for (Map.Entry<String, Double> e : series.entrySet()) {
            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setJustifyContentMode(JustifyContentMode.BETWEEN);

            Span left = new Span(e.getKey());
            Span right = new Span(String.format("€ %.2f", e.getValue()));

            row.add(left, right);
            row.getStyle().set("padding", "0.25rem 0");

            box.add(row);
        }

        list.add(box);
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
