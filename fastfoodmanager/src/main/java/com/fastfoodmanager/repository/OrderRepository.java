package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAssignedTo(String assignedTo);
}
