package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.repository.ProductRepository;
import com.fastfoodmanager.repository.FoodTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;
    private final ProductRepository productRepository;

    public FoodTypeService(FoodTypeRepository foodTypeRepository,
                           ProductRepository productRepository) {
        this.foodTypeRepository = foodTypeRepository;
        this.productRepository = productRepository;
    }

    // -------- CRUD BÁSICO --------

    public List<FoodType> findAll() {
        return foodTypeRepository.findAll();
    }

    public FoodType save(FoodType foodType) {
        // ⚡ Spring Data JPA guardará el campo 'image' automáticamente
        return foodTypeRepository.save(foodType);
    }

    // -------- LÓGICA DE NEGOCIO --------

    public void deleteFoodType(FoodType foodType) {

        // 1️⃣ Obtener productos de la categoría
        List<Product> products = productRepository.findByType(foodType);

        // 2️⃣ Desactivar productos y romper la referencia
        for (Product p : products) {
            p.setActive(false);
            p.setType(null);  // 🔑 importante
        }
        productRepository.saveAll(products);

        // 3️⃣ Eliminar la categoría
        foodTypeRepository.delete(foodType);
    }
}
