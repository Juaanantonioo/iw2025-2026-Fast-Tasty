package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.CashClosure;
import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.repository.CashClosureRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.*;
import java.util.*;

@Service
public class CashService {

    private final CashClosureRepository closureRepo;
    private final OrderService orderService;

    public CashService(CashClosureRepository closureRepo, OrderService orderService) {
        this.closureRepo = closureRepo;
        this.orderService = orderService;
    }

    public record CashSnapshot(
            double currentAmount,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<CashClosure> closuresToday
    ) {}

    public CashSnapshot getTodaySnapshot() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start = lastCloseOrStartOfDay(today);
        double amount = sumOrdersBetween(start, now);

        List<CashClosure> todayClosures = closureRepo.findByBusinessDate(today).stream()
                .sorted(Comparator.comparing(CashClosure::getClosedAt).reversed())
                .toList();

        return new CashSnapshot(amount, start, now, todayClosures);
    }

    public CashClosure closeCash(String operatorUsername) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start = lastCloseOrStartOfDay(today);
        double amount = sumOrdersBetween(start, now);

        CashClosure cc = new CashClosure(
                operatorUsername == null ? "operario" : operatorUsername,
                today,
                start,
                now,
                amount,
                now
        );

        return closureRepo.save(cc);
    }

    /**
     * Totales por DÍA dentro de un rango [from, to] INCLUSIVO.
     * - Rellena TODOS los días del rango con 0.0, para que en MES salgan 28/30/31 días.
     * - Suma importes de CashClosure dentro del rango.
     */
    public Map<LocalDate, Double> getClosedTotalsByDay(LocalDate from, LocalDate to) {
        if (from == null || to == null) return Map.of();

        if (to.isBefore(from)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }

        Map<LocalDate, Double> map = new LinkedHashMap<>();
        LocalDate d = from;
        while (!d.isAfter(to)) {
            map.put(d, 0.0);
            d = d.plusDays(1);
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();

        List<CashClosure> closures = closureRepo.findByClosedAtBetween(start, endExclusive);
        for (CashClosure c : closures) {
            if (c == null) continue;

            LocalDate day = (c.getBusinessDate() != null)
                    ? c.getBusinessDate()
                    : (c.getClosedAt() != null ? c.getClosedAt().toLocalDate() : null);

            if (day == null) continue;
            if (day.isBefore(from) || day.isAfter(to)) continue;

            map.put(day, map.getOrDefault(day, 0.0) + safeDouble(c.getAmount()));
        }

        return map;
    }

    public Map<DayOfWeek, Double> getClosedTotalsByDayOfWeek(LocalDateTime from, LocalDateTime to) {
        Map<DayOfWeek, Double> map = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) map.put(d, 0.0);

        for (CashClosure c : closureRepo.findByClosedAtBetween(from, to)) {
            DayOfWeek dow = c.getClosedAt().getDayOfWeek();
            map.put(dow, map.get(dow) + safeDouble(c.getAmount()));
        }
        return map;
    }

    private LocalDateTime lastCloseOrStartOfDay(LocalDate day) {
        LocalDateTime startOfDay = day.atStartOfDay();

        // último cierre del propio día (si existe)
        List<CashClosure> todayClosures = closureRepo.findByBusinessDate(day);
        if (!todayClosures.isEmpty()) {
            return todayClosures.stream()
                    .map(CashClosure::getClosedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(startOfDay);
        }
        return startOfDay;
    }

    private double sumOrdersBetween(LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderService.findAll();

        return orders.stream()
                .filter(Objects::nonNull)
                .filter(o -> isCountable(o))
                .filter(o -> {
                    LocalDateTime t = orderTime(o);
                    return t != null && !t.isBefore(from) && !t.isAfter(to);
                })
                .mapToDouble(o -> safeDouble(getDouble(o, "getTotal")))
                .sum();
    }

    /**
     * Criterio de “facturado”:
     * 1) Si existe isPaid() y es true -> cuenta
     * 2) Si no existe isPaid(), cuenta si status == ENTREGADO (o PAGADO si lo tuvieras)
     */
    private boolean isCountable(Order o) {
        Boolean paid = getBoolean(o, "isPaid");
        if (paid != null) return paid;

        String st = getString(o, "getStatus");
        if (st == null) return false;
        String s = st.trim().toUpperCase();
        return s.equals("ENTREGADO") || s.equals("PAGADO");
    }

    /**
     * Momento del pedido para caja:
     * 1) Si existe getPaidAt() -> usarlo
     * 2) Si no, usar getCreatedAt()
     */
    private LocalDateTime orderTime(Order o) {
        LocalDateTime paidAt = getDateTime(o, "getPaidAt");
        if (paidAt != null) return paidAt;

        return getDateTime(o, "getCreatedAt");
    }

    // ---------- Reflection helpers (para no depender de campos exactos) ----------

    private String getString(Object target, String methodName) {
        Object v = invoke(target, methodName);
        return (v instanceof String s) ? s : null;
    }

    private Double getDouble(Object target, String methodName) {
        Object v = invoke(target, methodName);
        if (v instanceof Double d) return d;
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }

    private LocalDateTime getDateTime(Object target, String methodName) {
        Object v = invoke(target, methodName);
        return (v instanceof LocalDateTime dt) ? dt : null;
    }

    private Boolean getBoolean(Object target, String methodName) {
        Object v = invoke(target, methodName);
        return (v instanceof Boolean b) ? b : null;
    }

    private Object invoke(Object target, String methodName) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod(methodName);
            return m.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private double safeDouble(Double d) {
        return d == null ? 0.0 : d;
    }
}
