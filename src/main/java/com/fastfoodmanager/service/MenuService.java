package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MenuService {
    private final ProductRepository repo;
    public MenuService(ProductRepository repo) { this.repo = repo; }

    public List<Product> findActiveProducts() {
        return repo.findByActiveTrue();
    }
}
