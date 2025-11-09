package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Operario: pedidos asignados a él
    List<Order> findByAssignedTo(String assignedTo);

    // Cliente: por username del cliente
    List<Order> findByCustomer_Username(String username);

    // Pendientes para cocina/reparto
    List<Order> findByStatusNot(String status);
}
