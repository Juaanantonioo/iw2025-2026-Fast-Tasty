package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.repository.ProductRepository;
import com.fastfoodmanager.repository.FoodTypeRepository;
import com.fastfoodmanager.repository.AllergenRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;
    private final ProductRepository productRepository;

    public FoodTypeService(FoodTypeRepository foodTypeRepository,
                           ProductRepository productRepository) {
        this.foodTypeRepository = foodTypeRepository;
        this.productRepository = productRepository;
    }

    public List<FoodType> findAll() {
        return foodTypeRepository.findAll();
    }

    public FoodType save(FoodType foodType) {
        return foodTypeRepository.save(foodType);
    }

    public void deleteFoodType(FoodType foodType) {
        // Eliminar primero los productos
        List<Product> products = productRepository.findByType(foodType);
        productRepository.deleteAll(products);

        // Luego eliminar el foodType
        foodTypeRepository.delete(foodType);
    }
}
