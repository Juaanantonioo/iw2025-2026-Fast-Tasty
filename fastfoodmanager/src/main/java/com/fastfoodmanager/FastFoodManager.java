package com.fastfoodmanager;

import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.ProductService;
import com.fastfoodmanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.fastfoodmanager.repository.AllergenRepository;
import com.fastfoodmanager.repository.FoodTypeRepository;

import java.util.List;

@SpringBootApplication
public class FastFoodManager {
    public static void main(String[] args) {
        SpringApplication.run(FastFoodManager.class, args);
    }
}
