package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.Order.Status;
import com.fastfoodmanager.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public List<Order> forOperator(String username) {
        return repo.findByAssignedTo(username);
    }

    public List<Order> findAll() { return repo.findAll(); }

    public void updateStatus(Long id, Status status) {
        repo.findById(id).ifPresent(o -> {
            o.setStatus(status);
            repo.save(o);
        });
    }

    public Order save(Order o) { return repo.save(o); }
    public long count() { return repo.count(); }
}
