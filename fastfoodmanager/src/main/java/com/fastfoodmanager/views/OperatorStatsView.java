package com.fastfoodmanager.views;

import com.fastfoodmanager.service.CashService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@PageTitle("Estadísticas | Operario")
@RolesAllowed("OPERATOR")
@Route(value = "operator/estadisticas", layout = MainLayout.class)
public class OperatorStatsView extends VerticalLayout {

    private final CashService cashService;

    private final ComboBox<String> filtro = new ComboBox<>("Filtro");
    private final DatePicker baseDate = new DatePicker("Fecha base");
    private final Button aplicar = new Button("Aplicar");

    private final Span rangoInfo = new Span();
    private final Div chartWrap = new Div();
    private final Div listWrap = new Div();

    private final DecimalFormat eurFmt =
            new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("es", "ES")));
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public OperatorStatsView(CashService cashService) {
        this.cashService = cashService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Estadísticas de caja");
        title.getStyle().set("margin", "0");

        filtro.setItems("SEMANA", "MES", "AÑO");
        filtro.setValue("SEMANA");
        filtro.setWidth("220px");

        baseDate.setValue(LocalDate.now());
        baseDate.setWidth("220px");

        aplicar.addClickListener(e -> refresh());

        HorizontalLayout controls = new HorizontalLayout(filtro, baseDate, aplicar);
        controls.setDefaultVerticalComponentAlignment(Alignment.END);
        controls.setSpacing(true);

        rangoInfo.getStyle().set("color", "#555").set("margin-top", "8px");

        chartWrap.setWidthFull();
        listWrap.setWidthFull();

        add(title, controls, rangoInfo, chartWrap, listWrap);

        refresh();
    }

    private void refresh() {
        String f = filtro.getValue() == null ? "SEMANA" : filtro.getValue();
        LocalDate base = baseDate.getValue() == null ? LocalDate.now() : baseDate.getValue();

        Range range = computeRange(f, base);

        // cierres por día (relleno a 0.0)
        Map<LocalDate, Double> byDay = cashService.getClosedTotalsByDay(range.from, range.to);

        Series series = buildSeries(f, range, byDay);

        rangoInfo.setText("Mostrando " + f + ": " + range.from.format(dateFmt) + " \u2192 " + range.to.format(dateFmt));

        chartWrap.removeAll();
        chartWrap.add(buildBarChart(series.labels, series.values, f));

        listWrap.removeAll();
        listWrap.add(buildList(series.labels, series.values));
    }

    private Range computeRange(String filtro, LocalDate base) {
        if ("MES".equals(filtro)) {
            LocalDate from = base.withDayOfMonth(1);
            LocalDate to = base.withDayOfMonth(base.lengthOfMonth());
            return new Range(from, to);
        }
        if ("AÑO".equals(filtro)) {
            LocalDate from = base.with(TemporalAdjusters.firstDayOfYear());
            LocalDate to = base.with(TemporalAdjusters.lastDayOfYear());
            return new Range(from, to);
        }

        // SEMANA
        LocalDate from = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate to = from.plusDays(6);
        return new Range(from, to);
    }

    private Series buildSeries(String filtro, Range range, Map<LocalDate, Double> byDay) {
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        if ("AÑO".equals(filtro)) {
            Map<Month, Double> monthSum = new LinkedHashMap<>();
            for (Month m : Month.values()) monthSum.put(m, 0.0);

            LocalDate d = range.from;
            while (!d.isAfter(range.to)) {
                double v = byDay.getOrDefault(d, 0.0);
                monthSum.put(d.getMonth(), monthSum.get(d.getMonth()) + v);
                d = d.plusDays(1);
            }

            for (Month m : Month.values()) {
                labels.add(monthNameEs(m));
                values.add(monthSum.get(m));
            }

            return new Series(labels, values);
        }

        // SEMANA o MES: serie por día
        LocalDate d = range.from;
        while (!d.isAfter(range.to)) {
            if ("SEMANA".equals(filtro)) labels.add(dayNameEs(d.getDayOfWeek()));
            else labels.add(String.valueOf(d.getDayOfMonth()));

            values.add(byDay.getOrDefault(d, 0.0));
            d = d.plusDays(1);
        }

        return new Series(labels, values);
    }

    private Div buildBarChart(List<String> labels, List<Double> values, String filtro) {
        Div root = new Div();
        root.setWidthFull();
        root.getStyle()
                .set("background", "white")
                .set("border", "1px solid rgba(0,0,0,0.08)")
                .set("border-radius", "12px")
                .set("padding", "16px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.03)");

        String unidad = "AÑO".equals(filtro) ? "mes" : "día";

        H3 h = new H3("Gráfico (€/ " + unidad + ")");
        h.getStyle().set("margin", "0 0 12px 0");
        root.add(h);

        double max = 0.0;
        for (Double v : values) if (v != null && v > max) max = v;
        if (max <= 0.0) max = 1.0;

        int maxBarPx = 220;

        Div scroller = new Div();
        scroller.setWidthFull();
        scroller.getStyle().set("overflow-x", "auto");

        Div bars = new Div();
        bars.getStyle()
                .set("display", "flex")
                .set("align-items", "flex-end")
                .set("gap", "10px")
                .set("padding", "8px 4px");

        String barMinWidth;
        if ("SEMANA".equals(filtro)) barMinWidth = "56px";
        else if ("AÑO".equals(filtro)) barMinWidth = "56px";
        else barMinWidth = "30px"; // MES

        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            double v = values.get(i) == null ? 0.0 : values.get(i);

            int hPx = (int) Math.round((v / max) * maxBarPx);
            if (hPx < 2) hPx = 2;

            Div col = new Div();
            col.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("min-width", barMinWidth);

            Span valueTxt = new Span("€ " + eurFmt.format(v));
            valueTxt.getStyle()
                    .set("font-size", "0.75rem")
                    .set("color", "#444")
                    .set("margin-bottom", "6px")
                    .set("white-space", "nowrap");

            Div bar = new Div();
            bar.getStyle()
                    .set("width", "100%")
                    .set("height", hPx + "px")
                    .set("border-radius", "10px 10px 6px 6px")
                    .set("background", "linear-gradient(180deg, #2f80ed, #1b63c6)")
                    .set("box-shadow", "0 2px 6px rgba(0,0,0,0.10)");

            Span labelTxt = new Span(label);
            labelTxt.getStyle()
                    .set("font-size", "0.78rem")
                    .set("color", "#333")
                    .set("margin-top", "8px")
                    .set("white-space", "nowrap");

            col.add(valueTxt, bar, labelTxt);
            bars.add(col);
        }

        scroller.add(bars);
        root.add(scroller);
        return root;
    }

    private Div buildList(List<String> labels, List<Double> values) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("margin-top", "14px")
                .set("background", "white")
                .set("border", "1px solid rgba(0,0,0,0.08)")
                .set("border-radius", "12px")
                .set("padding", "14px");

        double total = 0.0;
        for (Double v : values) total += (v == null ? 0.0 : v);

        Span totalTxt = new Span("Total del periodo: € " + eurFmt.format(total));
        totalTxt.getStyle()
                .set("font-weight", "700")
                .set("display", "inline-block")
                .set("margin-bottom", "10px");

        Div rows = new Div();
        rows.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr auto")
                .set("row-gap", "8px")
                .set("column-gap", "18px");

        for (int i = 0; i < labels.size(); i++) {
            String l = labels.get(i);
            double v = values.get(i) == null ? 0.0 : values.get(i);

            Span left = new Span(l);
            Span right = new Span("€ " + eurFmt.format(v));
            right.getStyle().set("font-variant-numeric", "tabular-nums");

            rows.add(left, right);
        }

        card.add(totalTxt, rows);
        return card;
    }

    private String dayNameEs(DayOfWeek d) {
        switch (d) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return d.name();
        }
    }

    private String monthNameEs(Month m) {
        switch (m) {
            case JANUARY: return "Enero";
            case FEBRUARY: return "Febrero";
            case MARCH: return "Marzo";
            case APRIL: return "Abril";
            case MAY: return "Mayo";
            case JUNE: return "Junio";
            case JULY: return "Julio";
            case AUGUST: return "Agosto";
            case SEPTEMBER: return "Septiembre";
            case OCTOBER: return "Octubre";
            case NOVEMBER: return "Noviembre";
            case DECEMBER: return "Diciembre";
            default: return m.name();
        }
    }

    private static class Range {
        final LocalDate from;
        final LocalDate to;
        Range(LocalDate from, LocalDate to) { this.from = from; this.to = to; }
    }

    private static class Series {
        final List<String> labels;
        final List<Double> values;
        Series(List<String> labels, List<Double> values) { this.labels = labels; this.values = values; }
    }
}
