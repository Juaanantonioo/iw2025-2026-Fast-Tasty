package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByAssignedTo(String assignedTo);

    List<Order> findByCustomer_Username(String username);

    List<Order> findByStatusAndAssignedToIsNull(String status);

    // Para "Mis pedidos" (cargar items + product)
    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByCustomer_UsernameOrderByCreatedAtDesc(String username);

    // Cocina con items
    @Query("""
           select distinct o
           from Order o
           left join fetch o.items i
           left join fetch i.product p
           where o.status = :status and o.cookedDone = false
           order by o.createdAt asc
           """)
    List<Order> findKitchenQueueWithItems(@Param("status") String status);

    List<Order> findByStatusAndCookedDoneFalse(String status);

    List<Order> findByDeliveryToAndStatus(String deliveryTo, String status);

    boolean existsByDeliveryToAndStatus(String deliveryTo, String status);

    List<Order> findByStatus(String status);

    List<Order> findByStatusNot(String status);

    // NUEVOS MÉTODOS NECESARIOS PARA LA FUNCIONALIDAD DE TIPOS DE PEDIDO

    // Para encontrar pedidos asignados a un operario con un estado específico
    List<Order> findByAssignedToAndStatus(String assignedTo, String status);

    // Para encontrar pedidos por tipo y estado (ej: PICKUP y LISTO)
    List<Order> findByOrderTypeAndStatus(OrderType orderType, String status);

    // Método adicional útil: buscar por tipo de pedido
    List<Order> findByOrderType(OrderType orderType);
}