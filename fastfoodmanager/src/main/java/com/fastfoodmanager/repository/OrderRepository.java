package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Pedido con items + product (para modificar/cancelar sin LazyInitialization)
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findWithItemsById(Long id);

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

    List<Order> findByAssignedToAndStatus(String assignedTo, String status);

    List<Order> findByOrderTypeAndStatus(OrderType orderType, String status);

    List<Order> findByOrderType(OrderType orderType);
}