package com.fastfoodmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.FoodType;
import com.fastfoodmanager.repository.ProductRepository;
import com.fastfoodmanager.repository.FoodTypeRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private FoodTypeRepository foodTypeRepo;

    @Override
    public List<Product> findAll() {
        return productRepo.findAll();
    }

    @Override
    public Product save(Product p) {
        return productRepo.save(p);
    }

    @Override
    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    @Override
    public List<FoodType> findAllFoodTypes() {
        return foodTypeRepo.findAll();
    }

    @Override
    @Transactional
    public void updateStock(Long id, int newStock) {
        Product p = productRepo.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Producto no encontrado con ID: " + id)
        );
        p.setStock(Math.max(0, newStock));
    }
}
