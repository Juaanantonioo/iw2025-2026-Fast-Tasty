package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.*;
import com.fastfoodmanager.repository.OrderRepository;
import com.fastfoodmanager.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepo;
    private final UserService userService;
    private final EmailService emailService;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo,
                        UserService userService,
                        EmailService emailService,
                        ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.userService = userService;
        this.emailService = emailService;
        this.productRepo = productRepo;
    }

    // =========
    // CRUD
    // =========
    public List<Order> findAll() { return orderRepo.findAll(); }

    public Optional<Order> findById(Long id) { return orderRepo.findById(id); }

    public Order save(Order order) { return orderRepo.save(order); }

    public void delete(Long id) { orderRepo.deleteById(id); }

    public long count() { return orderRepo.count(); }

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
    public Order createOrder(User user,
                             List<OrderItem> items,
                             OrderType orderType,
                             String deliveryAddress,
                             boolean enviarEmail) {

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

        order.setPaid(false);
        order.setPaidAt(null);

        order.setCookedDone(false);
        order.setCookedAt(null);
        order.setCookedBy(null);

        order.recalcTotal();

        order = orderRepo.save(order);
        log.info("Pedido #{} creado exitosamente", order.getId());

        if (enviarEmail && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            try {
                emailService.enviarTicketPedido(order, user.getEmail());
                log.info("Ticket enviado por email para el pedido #{}", order.getId());
            } catch (Exception e) {
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

    public List<Order> findForCurrentCustomerWithItems() {
        String username = currentUsername();
        if (username == null) return List.of();
        return orderRepo.findByCustomer_UsernameOrderByCreatedAtDesc(username);
    }

    // =========
    // Fetch pedido con items
    // =========
    @Transactional(readOnly = true)
    public Order findWithItemsOrThrow(Long orderId) {
        return orderRepo.findWithItemsById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + orderId));
    }

    // =========
    // Cliente: reglas de edición
    // =========
    private Order requireCustomerOrderEditable(Long orderId, String customerUsername) {
        Order o = orderRepo.findWithItemsById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado."));

        if (!customerUsername.equals(o.getCustomer().getUsername())) {
            throw new SecurityException("No puedes modificar pedidos de otro usuario.");
        }

        if (!o.isPaid()) {
            throw new IllegalStateException("Solo se puede modificar/cancelar un pedido si está pagado.");
        }

        if (!"ENVIADO".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("Solo se puede modificar/cancelar si el pedido está ENVIADO.");
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

        o.setStatus("CANCELADO");
        o.setAssignedTo(null);
        o.setDeliveryTo(null);

        orderRepo.save(o);
    }

    // =========
    // Cliente: modificar cantidades
    // =========
    @Transactional
    public void updatePaidOrderItemsBeforeKitchen(Long orderId, Map<Integer, Integer> indexToQty) {
        String username = currentUsername();
        if (username == null) throw new IllegalStateException("Usuario no autenticado.");

        Order o = requireCustomerOrderEditable(orderId, username);

        if (indexToQty == null || indexToQty.isEmpty()) {
            throw new IllegalArgumentException("No hay cambios que aplicar.");
        }

        List<OrderItem> items = o.getItems();

        for (Map.Entry<Integer, Integer> entry : indexToQty.entrySet()) {
            int index = entry.getKey();
            int qty = entry.getValue();

            if (index < 0 || index >= items.size()) continue;

            if (qty <= 0) {
                items.remove(index);
            } else {
                items.get(index).setQuantity(qty);
            }
        }

        o.setItems(items);
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

    // =========
    // Productos activos para añadir a un pedido
    // =========
    @Transactional(readOnly = true)
    public List<Product> findActiveProductsForEdit() {
        return productRepo.findAll().stream()
                .filter(Product::isActive)
                .toList();
    }

    // =========
    // Repartidor - cargar pedidos con detalles
    // =========
    @Transactional(readOnly = true)
    public List<Order> findForDeliveryWithDetails(String deliveryUsername) {
        List<Order> orders = orderRepo.findByDeliveryToAndStatus(deliveryUsername, "EN REPARTO");

        for (Order o : orders) {
            Order full = orderRepo.findWithItemsById(o.getId()).orElse(o);
            o.setItems(full.getItems());
        }
        return orders;
    }

    @Transactional
    public Order findDeliveryOrderWithDetails(Long orderId, String deliveryUsername) {

        // 1. Cargar pedido
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) return null;

        // 2. Validar repartidor
        if (order.getDeliveryTo() == null || !order.getDeliveryTo().equals(deliveryUsername)) {
            throw new IllegalStateException("Este pedido no está asignado a este repartidor");
        }

        // 3. Forzar carga del cliente (EVITA email null)
        if (order.getCustomer() != null) {
            order.getCustomer().getId();        // inicializa proxy
            order.getCustomer().getUsername();
            order.getCustomer().getEmail();     // 🔥 ESTA ES LA CLAVE
        }

        // 4. Forzar carga de items
        if (order.getItems() != null) {
            order.getItems().size();

            // 5. Reconstruir snapshots
            for (OrderItem item : order.getItems()) {

                if (item.isProduct() && item.getProduct() != null) {
                    item.getProduct().getName();
                }

                if (item.isMenu() && item.getMenu() != null) {
                    item.getMenu().getName();
                }
            }
        }

        return order;
    }

    // =========
    // Cocina
    // =========
    @Transactional
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

    // =========
    // Operario recoge pedidos listos
    // =========
    public List<Order> findReadyForOperator(String operatorUsername) {
        return orderRepo.findByAssignedToAndStatus(operatorUsername, "LISTO");
    }

    public List<Order> findReadyOrdersByType(OrderType orderType) {
        return orderRepo.findByOrderTypeAndStatus(orderType, "LISTO");
    }

    // =========
    // Repartidor
    // =========
    @Transactional
    public void assignFreeDeliveryAndSend(Long orderId, String operatorUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (!operatorUsername.equals(o.getAssignedTo())) {
                throw new IllegalStateException("Solo el operario asignado puede gestionar este pedido.");
            }
            if (!"LISTO".equalsIgnoreCase(o.getStatus())) {
                throw new IllegalStateException("Solo se puede asignar reparto cuando el pedido está LISTO.");
            }
            if (o.getOrderType() != OrderType.DELIVERY) {
                throw new IllegalStateException("Solo los pedidos a domicilio necesitan repartidor.");
            }

            List<User> deliveries = userService.findByRole(User.Role.DELIVERY);
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
    public Order markDelivered(Long orderId, String deliveryUsername) {

        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Pedido no encontrado"));

        if (!"EN REPARTO".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("Solo se puede entregar un pedido EN REPARTO.");
        }
        if (!deliveryUsername.equals(o.getDeliveryTo())) {
            throw new IllegalStateException("Este pedido no está asignado a ti.");
        }

        o.setStatus("ENTREGADO");
        orderRepo.save(o);

        return o; // 🔥 DEVOLVER EL PEDIDO
    }

    // =========
    // Valoración
    // =========
    @Transactional
    public void saveRating(Long orderId, int stars, String username) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("La valoración debe ser entre 1 y 5 estrellas");
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // Validar que el pedido pertenece al usuario
        if (!order.getCustomer().getUsername().equals(username)) {
            throw new SecurityException("No puedes valorar un pedido que no es tuyo");
        }

        // Evitar doble valoración
        if (order.getRating() != null) {
            throw new IllegalStateException("Este pedido ya ha sido valorado");
        }

        order.setRating(stars);
        orderRepo.save(order);
    }

    // =========
    // Pickup
    // =========
    @Transactional
    public void markAsPickedUp(Long orderId, String operatorUsername) {
        orderRepo.findById(orderId).ifPresent(o -> {
            if (!operatorUsername.equals(o.getAssignedTo())) {
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

    @Transactional(readOnly = true)
    public Order findOrderWithDetails(Long orderId) {
        Order order = orderRepo.findWithItemsById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));

        // Forzar carga del cliente
        if (order.getCustomer() != null) {
            order.getCustomer().getUsername();
            order.getCustomer().getEmail();
        }

        // Forzar carga de ingredientes
        order.getItems().size(); // solo asegurar que la lista está cargada


        return order;
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

    public List<Order> findByDeliveryToAndStatus(String deliveryUsername, String status) {
        return orderRepo.findByDeliveryToAndStatus(deliveryUsername, status);
    }

}
