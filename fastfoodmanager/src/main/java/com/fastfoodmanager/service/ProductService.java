package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> findAll() {
        return repo.findAll();
    }

    public Product save(Product p) {
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    // 🔹 Nuevo método para actualizar el stock
    @Transactional
    public void updateStock(Long id, int newStock) {
        Product p = repo.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Producto no encontrado con ID: " + id));
        p.setStock(Math.max(0, newStock)); // evita valores negativos
        // gracias a @Transactional, no hace falta repo.save()
    }
}
