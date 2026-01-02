package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.FoodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Traer todos los productos activos con sus alérgenos (evitar LazyInitializationException)
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.allergens WHERE p.active = true")
    List<Product> findActiveWithAllergens();

    // Traer productos activos que NO tengan ninguno de los alérgenos pasados
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.allergens a " +
            "WHERE p.active = true AND (a IS NULL OR a.id NOT IN :allergenIds)")
    List<Product> findActiveWithoutAllergens(@Param("allergenIds") List<Long> allergenIds);

    // 🔴 NUEVO → Productos por FoodType (para borrado en cascada manual)
    List<Product> findByType(FoodType type);
}
