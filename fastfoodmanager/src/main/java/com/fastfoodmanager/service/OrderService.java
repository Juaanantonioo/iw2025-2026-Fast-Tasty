package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.repository.OrderRepository;
import com.fastfoodmanager.repository.ProductRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    // ---------- CRUD básico ----------
    public List<Order> findAll() { return orderRepo.findAll(); }

    public Optional<Order> findById(Long id) { return orderRepo.findById(id); }

    public Order save(Order order) { return orderRepo.save(order); }

    public void delete(Long id) { orderRepo.deleteById(id); }

    public long count() { return orderRepo.count(); }

    // ---------- Lógica de negocio ----------
    /**
     * Crea un pedido desde el carrito. Enlaza items, inicializa estado y
     * lo asigna por defecto a "operario1" para que sea visible al operario.
     */
    public Order createOrder(User user, List<OrderItem> items) {
        Order order = new Order(user, items);
        if (items != null) {
            for (OrderItem item : items) {
                if (item != null) item.setOrder(order);
            }
        }
        order.setStatus("EN COCINA");
        order.setAssignedTo("operario1"); // quita esta línea si no quieres asignación automática
        order.recalcTotal();
        return orderRepo.save(order);
    }

    /** Cambia el estado de un pedido */
    public void updateStatus(Long orderId, String newStatus) {
        orderRepo.findById(orderId).ifPresent(o -> {
            o.setStatus(newStatus);
            orderRepo.save(o);
        });
    }

    /** Marca como pagado */
    public void markAsPaid(Long orderId) {
        updateStatus(orderId, "PAGADO");
    }

    // ---------- Consultas por rol ----------
    /** Pedidos para un operario concreto (por su username) */
    public List<Order> findForOperator(String operatorUsername) {
        if (operatorUsername == null || operatorUsername.isBlank()) {
            return Collections.emptyList();
        }
        return orderRepo.findByAssignedTo(operatorUsername);
    }

    /**
     * Pedidos de un cliente por username. Usa el método del repositorio y,
     * si no existiera en tu repo por cualquier motivo, hace un fallback
     * por filtro en memoria (case-insensitive).
     */
    public List<Order> findForCustomer(String customerUsername) {
        if (customerUsername == null || customerUsername.isBlank()) {
            return Collections.emptyList();
        }
        try {
            // Repositorio recomendado:
            return orderRepo.findByCustomer_Username(customerUsername);
        } catch (Throwable ignored) {
            // Fallback seguro por si el método no existiera en tu repo
            String u = customerUsername.toLowerCase();
            return orderRepo.findAll().stream()
                    .filter(o -> o.getCustomer() != null && o.getCustomer().getUsername() != null)
                    .filter(o -> o.getCustomer().getUsername().equalsIgnoreCase(u)
                            || o.getCustomer().getUsername().equalsIgnoreCase(customerUsername))
                    .toList();
        }
    }

    /** Pedidos del usuario autenticado actualmente */
    public List<Order> findForCurrentCustomer() {
        String username = getCurrentUsername();
        return (username == null) ? List.of() : findForCustomer(username);
    }

    /** Pedidos no entregados (útil para listados generales) */
    public List<Order> findPending() {
        return orderRepo.findByStatusNot("ENTREGADO");
    }

    // ---------- Helpers ----------
    /** Devuelve el username autenticado o null si no hay sesión */
    private String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || Objects.equals("anonymousUser", String.valueOf(a.getPrincipal()))) {
            return null;
        }
        return a.getName();
    }
}
