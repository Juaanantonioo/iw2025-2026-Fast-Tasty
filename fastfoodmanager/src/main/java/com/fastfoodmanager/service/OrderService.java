package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.OrderType;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepo;
    private final UserService userService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepo, UserService userService, EmailService emailService) {
        this.orderRepo = orderRepo;
        this.userService = userService;
        this.emailService = emailService;
    }

    // =========
    // CRUD
    // =========
    public List<Order> findAll() {
        return orderRepo.findAll();
    }

    public Optional<Order> findById(Long id) {
        return orderRepo.findById(id);
    }

    public Order save(Order order) {
        return orderRepo.save(order);
    }

    public void delete(Long id) {
        orderRepo.deleteById(id);
    }

    public long count() {
        return orderRepo.count();
    }

    // =========
    // Auth helper
    // =========
    private String currentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return null;
        String u = a.getName();
        return (u == null || "anonymousUser".equals(u)) ? null : u;
    }

    // =========
    // Crear pedido
    // =========
    @Transactional
    public Order createOrder(User user, List<OrderItem> items, OrderType orderType, String deliveryAddress, boolean enviarEmail) {
        Order order = new Order(user, items);
        order.setOrderType(orderType);

        if (orderType == OrderType.DELIVERY && deliveryAddress != null) {
            order.setDeliveryAddress(deliveryAddress);
        }

        if (items != null) {
            for (OrderItem item : items) {
                item.setOrder(order);
            }
        }

        order.setStatus("ENVIADO");
        order.setAssignedTo(null);
        order.setDeliveryTo(null);
        order.setPickedUpAt(null);

        // pago simulado: por defecto NO pagado hasta que el usuario "pague"
        order.setPaid(false);
        order.setPaidAt(null);

        order.setCookedDone(false);
        order.setCookedAt(null);
        order.setCookedBy(null);

        order.recalcTotal();

        // IMPORTANTE: Guardar primero el pedido para obtener el ID
        order = orderRepo.save(order);
        log.info("Pedido #{} creado exitosamente", order.getId());

        // Enviar email si está solicitado
        if (enviarEmail && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            try {
                emailService.enviarTicketPedido(order, user.getEmail());
                log.info("Ticket enviado por email para el pedido #{}", order.getId());
            } catch (Exception e) {
                // Loggear error pero no fallar el pedido
                log.error("Error al enviar email del pedido #{}: {}", order.getId(), e.getMessage());
            }
        }

        return order;
    }

    @Transactional
    public Order createOrder(User user, List<OrderItem> items, OrderType orderType, String deliveryAddress) {
        return createOrder(user, items, orderType, deliveryAddress, false);
    }

    public Order createOrder(User user, List<OrderItem> items) {
        return createOrder(user, items, OrderType.PICKUP, null, false);
    }

    // =========
    // Pago simulado
    // =========
    @Transactional
    public void markAsPaid(Long orderId) {
        orderRepo.findById(orderId).ifPresent(o -> {
            o.setPaid(true);
            o.setPaidAt(LocalDateTime.now());
            orderRepo.save(o);
        });
    }

    // =========
    // Cliente - Mis pedidos
    // =========
    public List<Order> findForCustomer(String customerUsername) {
        return orderRepo.findByCustomer_Username(customerUsername);
    }

    public List<Order> findForCurrentCustomer() {
        String username = currentUsername();
        return (username == null) ? List.of() : findForCustomer(username);
    }

    // Cargar pedidos del cliente con items+product
    public List<Order> findForCurrentCustomerWithItems() {
        String username = currentUsername();
        if (username == null) return List.of();
        return orderRepo.findByCustomer_UsernameOrderByCreatedAtDesc(username);
    }

    // =========
    // Fetch pedido con items+product (evita LazyInitialization)
    // =========
    @Transactional(readOnly = true)
    public Order findWithItemsOrThrow(Long orderId) {
        return orderRepo.findWithItemsById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + orderId));
    }

    // =========
    // Métodos para repartidores - cargan todo en una transacción
    // =========

    /**
     * Método para repartidores - carga pedido con customer, items y products
     * TODO en una transacción para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public Order findDeliveryOrderWithDetails(Long orderId, String deliveryUsername) {
        Order order = orderRepo.findWithItemsById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + orderId));

        // Verificar que el pedido está asignado a este repartidor
        if (order.getDeliveryTo() == null || !deliveryUsername.equals(order.getDeliveryTo())) {
            throw new SecurityException("Este pedido no está asignado a ti");
        }

        // Forzar la inicialización del cliente para evitar LazyInitialization
        if (order.getCustomer() != null) {
            // Esto inicializa el proxy
            order.getCustomer().getUsername();
        }

        // Forzar inicialización de items y productos
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    item.getProduct().getName(); // Inicializa el producto
                }
            }
        }

        return order;
    }

    /**
     * Método para cargar todos los pedidos del repartidor con detalles
     * TODO en una transacción para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public List<Order> findForDeliveryWithDetails(String deliveryUsername) {
        List<Order> orders = orderRepo.findByDeliveryToAndStatus(deliveryUsername, "EN REPARTO");

        // Inicializar relaciones para cada pedido
        for (Order order : orders) {
            if (order.getCustomer() != null) {
                order.getCustomer().getUsername(); // Inicializa cliente
            }

            // Cargar items y productos usando una consulta separada
            Order fullOrder = orderRepo.findWithItemsById(order.getId()).orElse(order);
            order.setItems(fullOrder.getItems());
        }

        return orders;
    }

    // =========
    // Cliente: reglas de edición
    // =========
    private Order requireCustomerOrderEditable(Long orderId, String customerUsername) {
        // IMPORTANTE: lo cargamos con items+product para poder modificar sin LazyInitialization
        Order o = orderRepo.findWithItemsById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado."));

        if (o.getCustomer() == null || o.getCustomer().getUsername() == null
                || !customerUsername.equals(o.getCustomer().getUsername())) {
            throw new SecurityException("No puedes modificar pedidos de otro usuario.");
        }

        if (!o.isPaid()) {
            throw new IllegalStateException("Solo se puede modificar/cancelar un pedido si está pagado.");
        }

        if (o.getStatus() == null || !"ENVIADO".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("Solo se puede modificar/cancelar si el pedido está ENVIADO (antes de cocina).");
        }

        return o;
    }

    // =========
    // Cliente: cancelar antes de cocina
    // =========
    @Transactional
    public void cancelPaidOrderBeforeKitchen(Long orderId) {
        String username = currentUsername();
        if (username == null) throw new IllegalStateException("Usuario no autenticado.");

        Order o = requireCustomerOrderEditable(orderId, username);

        // Recomendado: NO borrar; marcar cancelado
        o.setStatus("CANCELADO");

        // Limpieza opcional para que no aparezca en bandejas
        o.setAssignedTo(null);
        o.setDeliveryTo(null);

        // Si quieres "simular devolución", puedes descomentar:
        // o.setPaid(false);
        // o.setPaidAt(null);

        orderRepo.save(o);
    }

    // =========
    // Cliente: modificar cantidades antes de cocina (SIN quitar el pagado)
    // =========
    @Transactional
    public void updatePaidOrderItemsBeforeKitchen(Long orderId, Map<Long, Integer> productIdToQty) {
        String username = currentUsername();
        if (username == null) throw new IllegalStateException("Usuario no autenticado.");

        if (productIdToQty == null || productIdToQty.isEmpty()) {
            throw new IllegalArgumentException("No hay cambios que aplicar.");
        }

        Order o = requireCustomerOrderEditable(orderId, username);

        if (o.getItems() == null) {
            throw new IllegalStateException("El pedido no tiene items.");
        }

        // Ajustar cantidades / eliminar líneas con qty <= 0
        Iterator<OrderItem> it = o.getItems().iterator();
        while (it.hasNext()) {
            OrderItem item = it.next();
            Long pid = (item.getProduct() != null) ? item.getProduct().getId() : null;
            if (pid == null) continue;

            if (!productIdToQty.containsKey(pid)) continue;

            Integer newQty = productIdToQty.get(pid);
            if (newQty == null || newQty <= 0) {
                it.remove();
            } else {
                item.setQuantity(newQty);
            }
        }

        // Asegurar bidireccionalidad
        o.getItems().forEach(i -> i.setOrder(o));

        // Recalcular total
        o.recalcTotal();

        // CLAVE: NO tocamos paid aquí, para que puedas modificar varias veces mientras esté ENVIADO.
        orderRepo.save(o);
    }

    // =========
    // Operario
    // =========
    public List<Order> findForOperatorInbox(String operatorUsername) {
        List<Order> unassigned = orderRepo.findByStatusAndAssignedToIsNull("ENVIADO");
        List<Order> mine = orderRepo.findByAssignedTo(operatorUsername);

        Map<Long, Order> map = new LinkedHashMap<>();
        for (Order o : unassigned) map.put(o.getId(), o);
        for (Order o : mine) map.put(o.getId(), o);
        return new ArrayList<>(map.values());
    }

    @Transactional
    public void sendToKitchen(Long orderId, String operatorUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (!"ENVIADO".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("Solo se puede mandar a cocina un pedido ENVIADO.");
            }
            if ("CANCELADO".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("No se puede mandar a cocina un pedido cancelado.");
            }

            if (o.getAssignedTo() == null) o.setAssignedTo(operatorUsername);
            if (!operatorUsername.equals(o.getAssignedTo())) {
                throw new IllegalStateException("Este pedido ya está siendo gestionado por otro operario.");
            }

            o.setStatus("EN COCINA");
            orderRepo.save(o);
        });
    }

    public List<Order> findForCook() {
        return orderRepo.findKitchenQueueWithItems("EN COCINA");
    }

    @Transactional
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

    public List<Order> findReadyForOperator(String operatorUsername) {
        return orderRepo.findByAssignedToAndStatus(operatorUsername, "LISTO");
    }

    public List<Order> findReadyOrdersByType(OrderType orderType) {
        return orderRepo.findByOrderTypeAndStatus(orderType, "LISTO");
    }

    // =========
    // Repartidor
    // =========
    public List<Order> findForDelivery(String deliveryUsername) {
        return orderRepo.findByDeliveryToAndStatus(deliveryUsername, "EN REPARTO");
    }

    @Transactional
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

            if (free == null) throw new IllegalStateException("No hay repartidores libres ahora mismo.");

            o.setDeliveryTo(free);
            o.setStatus("EN REPARTO");
            orderRepo.save(o);
        });
    }

    @Transactional
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

    // =========
    // Pickup
    // =========
    @Transactional
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

    // =========
    // Compatibilidad updateStatus
    // =========
    @Transactional
    public void updateStatus(Long orderId, String newStatus) {
        orderRepo.findById(orderId).ifPresent(o -> {
            String ns = (newStatus == null) ? "" : newStatus.trim().toUpperCase();

            if ("PAGADO".equals(ns)) {
                o.setPaid(true);
                o.setPaidAt(LocalDateTime.now());
                orderRepo.save(o);
                return;
            }

            if (List.of("ENVIADO", "EN COCINA", "LISTO", "EN REPARTO", "ENTREGADO", "RECOGIDO", "CANCELADO").contains(ns)) {
                o.setStatus(ns);
                orderRepo.save(o);
                return;
            }

            throw new IllegalArgumentException("Estado no soportado: " + newStatus);
        });
    }
}