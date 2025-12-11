package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.FoodType;

import java.util.List;

public interface ProductService {

    List<Product> findAll();

    Product save(Product p);

    void delete(Long id);

    List<FoodType> findAllFoodTypes();

    void updateStock(Long id, int newStock);
}
