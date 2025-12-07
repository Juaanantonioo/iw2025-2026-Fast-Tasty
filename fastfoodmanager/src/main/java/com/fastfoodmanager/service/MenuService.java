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
public class MenuService {

    private final ProductRepository productRepo;
    private final FoodTypeRepository foodTypeRepo;
    private final AllergenRepository allergenRepo;

    public MenuService(ProductRepository productRepo,
                       FoodTypeRepository foodTypeRepo,
                       AllergenRepository allergenRepo) {
        this.productRepo = productRepo;
        this.foodTypeRepo = foodTypeRepo;
        this.allergenRepo = allergenRepo;
    }

    // Traer todos los productos activos con alérgenos cargados
    public List<Product> findActiveProducts() {
        return productRepo.findActiveWithAllergens();
    }

    // Traer productos activos excluyendo los que contienen ciertos alérgenos
    public List<Product> findActiveWithoutAllergens(List<Long> allergenIds) {
        if (allergenIds == null || allergenIds.isEmpty()) {
            return productRepo.findActiveWithAllergens();
        }
        return productRepo.findActiveWithoutAllergens(allergenIds);
    }

    public List<FoodType> findAllFoodTypes() {
        return foodTypeRepo.findAll();
    }

    public List<Allergen> findAllAllergens() {
        return allergenRepo.findAll();
    }
}
