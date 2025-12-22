package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.OrderType;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.OrderRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final UserService userService;

    public OrderService(OrderRepository orderRepo, UserService userService) {
        this.orderRepo = orderRepo;
        this.userService = userService;
    }

    // CRUD
    public List<Order> findAll() { return orderRepo.findAll(); }
    public Optional<Order> findById(Long id) { return orderRepo.findById(id); }
    public Order save(Order order) { return orderRepo.save(order); }
    public void delete(Long id) { orderRepo.deleteById(id); }
    public long count() { return orderRepo.count(); }

    // Crear pedido: queda ENVIADO hasta que el operario lo mande a cocina
    public Order createOrder(User user, List<OrderItem> items, OrderType orderType, String deliveryAddress) {
        Order order = new Order(user, items);
        order.setOrderType(orderType);

        if (orderType == OrderType.DELIVERY && deliveryAddress != null) {
            order.setDeliveryAddress(deliveryAddress);
        }

        for (OrderItem item : items) item.setOrder(order);

        order.setStatus("ENVIADO");
        order.setAssignedTo(null);    // aún no hay operario asignado
        order.setDeliveryTo(null);    // aún no hay repartidor
        order.setPickedUpAt(null);    // aún no recogido

        order.setPaid(false);
        order.setPaidAt(null);

        order.setCookedDone(false);
        order.setCookedAt(null);
        order.setCookedBy(null);

        order.recalcTotal();
        return orderRepo.save(order);
    }

    // Sobrecarga para compatibilidad
    public Order createOrder(User user, List<OrderItem> items) {
        return createOrder(user, items, OrderType.PICKUP, null);
    }

    // Pago simulado SIN tocar el estado
    public void markAsPaid(Long orderId) {
        orderRepo.findById(orderId).ifPresent(o -> {
            o.setPaid(true);
            o.setPaidAt(LocalDateTime.now());
            orderRepo.save(o);
        });
    }

    // Operario: bandeja (ENVIADO sin asignar) + los suyos
    public List<Order> findForOperatorInbox(String operatorUsername) {
        List<Order> unassigned = orderRepo.findByStatusAndAssignedToIsNull("ENVIADO");
        List<Order> mine = orderRepo.findByAssignedTo(operatorUsername);

        Map<Long, Order> map = new LinkedHashMap<>();
        for (Order o : unassigned) map.put(o.getId(), o);
        for (Order o : mine) map.put(o.getId(), o);
        return new ArrayList<>(map.values());
    }

    // Cliente
    public List<Order> findForCustomer(String customerUsername) {
        return orderRepo.findByCustomer_Username(customerUsername);
    }

    public List<Order> findForCurrentCustomer() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String username = (a != null) ? a.getName() : null;
        return (username == null) ? List.of() : findForCustomer(username);
    }

    // =======================
    // COCINA (con FETCH JOIN)
    // =======================
    public List<Order> findForCook() {
        return orderRepo.findKitchenQueueWithItems("EN COCINA");
    }

    // Repartidor: ver sus pedidos EN REPARTO
    public List<Order> findForDelivery(String deliveryUsername) {
        return orderRepo.findByDeliveryToAndStatus(deliveryUsername, "EN REPARTO");
    }

    // OPERARIO: ENVIADO -> EN COCINA (asigna el operario)
    public void sendToKitchen(Long orderId, String operatorUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (!"ENVIADO".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("Solo se puede mandar a cocina un pedido ENVIADO.");
            }
            if (o.getAssignedTo() == null) o.setAssignedTo(operatorUsername);

            if (!operatorUsername.equals(o.getAssignedTo())) {
                throw new IllegalStateException("Este pedido ya está siendo gestionado por otro operario.");
            }

            o.setStatus("EN COCINA");
            orderRepo.save(o);
        });
    }

    // COCINERO: EN COCINA -> LISTO
    public void markCookedDone(Long orderId, String cookUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (!"EN COCINA".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("Solo se puede marcar HECHO un pedido EN COCINA.");
            }
            o.setCookedDone(true);
            o.setCookedBy(cookUsername);
            o.setCookedAt(LocalDateTime.now());

            o.setStatus("LISTO");
            orderRepo.save(o);
        });
    }

    // OPERARIO: LISTO -> EN REPARTO (asignando un repartidor libre) - SOLO PARA DELIVERY
    public void assignFreeDeliveryAndSend(Long orderId, String operatorUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (o.getAssignedTo() == null || !operatorUsername.equals(o.getAssignedTo())) {
                throw new IllegalStateException("Solo el operario asignado puede gestionar este pedido.");
            }
            if (!"LISTO".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("Solo se puede asignar reparto cuando el pedido está LISTO.");
            }
            if (o.getOrderType() != OrderType.DELIVERY) {
                throw new IllegalStateException("Solo los pedidos a domicilio necesitan repartidor.");
            }

            List<User> deliveries = userService.findByRole(Role.DELIVERY);
            String free = deliveries.stream()
                    .map(User::getUsername)
                    .filter(u -> !orderRepo.existsByDeliveryToAndStatus(u, "EN REPARTO"))
                    .findFirst()
                    .orElse(null);

            if (free == null) {
                throw new IllegalStateException("No hay repartidores libres ahora mismo.");
            }

            o.setDeliveryTo(free);
            o.setStatus("EN REPARTO");
            orderRepo.save(o);
        });
    }

    // OPERARIO: Marcar como RECOGIDO (para pedidos PICKUP)
    public void markAsPickedUp(Long orderId, String operatorUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (o.getAssignedTo() == null || !operatorUsername.equals(o.getAssignedTo())) {
                throw new IllegalStateException("Solo el operario asignado puede gestionar este pedido.");
            }
            if (!"LISTO".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("Solo se puede marcar como recogido un pedido LISTO.");
            }
            if (o.getOrderType() != OrderType.PICKUP) {
                throw new IllegalStateException("Solo los pedidos para recoger en local pueden marcarse como recogidos.");
            }

            o.setStatus("RECOGIDO");
            o.setPickedUpAt(LocalDateTime.now());
            orderRepo.save(o);
        });
    }

    // Método para obtener pedidos LISTO que necesitan acción del operario
    public List<Order> findReadyForOperator(String operatorUsername) {
        // Busca pedidos LISTO que están asignados al operario
        return orderRepo.findByAssignedToAndStatus(operatorUsername, "LISTO");
    }

    // Método para obtener pedidos LISTO por tipo
    public List<Order> findReadyOrdersByType(OrderType orderType) {
        return orderRepo.findByOrderTypeAndStatus(orderType, "LISTO");
    }

    // REPARTIDOR: EN REPARTO -> ENTREGADO
    public void markDelivered(Long orderId, String deliveryUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (!"EN REPARTO".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("Solo se puede entregar un pedido EN REPARTO.");
            }
            if (o.getDeliveryTo() == null || !deliveryUsername.equals(o.getDeliveryTo())) {
                throw new IllegalStateException("Este pedido no está asignado a ti.");
            }
            o.setStatus("ENTREGADO");
            orderRepo.save(o);
        });
    }

    // Compatibilidad con código antiguo que llamaba updateStatus(...)
    public void updateStatus(Long orderId, String newStatus) {
        orderRepo.findById(orderId).ifPresent(o -> {
            String ns = (newStatus == null) ? "" : newStatus.trim().toUpperCase();

            // "PAGADO" antiguo
            if ("PAGADO".equals(ns)) {
                o.setPaid(true);
                o.setPaidAt(LocalDateTime.now());
                orderRepo.save(o);
                return;
            }

            // Estados soportados (incluyendo RECOGIDO)
            if (List.of("ENVIADO", "EN COCINA", "LISTO", "EN REPARTO", "ENTREGADO", "RECOGIDO").contains(ns)) {
                o.setStatus(ns);
                orderRepo.save(o);
                return;
            }

            throw new IllegalArgumentException("Estado no soportado: " + newStatus);
        });
    }

    public List<Order> findForCurrentCustomerWithItems() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String username = (a != null) ? a.getName() : null;
        if (username == null || "anonymousUser".equals(username)) return List.of();
        return orderRepo.findByCustomer_UsernameOrderByCreatedAtDesc(username);
    }

}