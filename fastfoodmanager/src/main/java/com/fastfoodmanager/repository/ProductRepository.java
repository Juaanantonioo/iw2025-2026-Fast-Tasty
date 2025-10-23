package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Buscamos la lista de los productos activos
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue(); // usado por MenuService
}
