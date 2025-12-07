package com.fastfoodmanager.bootstrap;

import com.fastfoodmanager.domain.*;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.AllergenRepository;
import com.fastfoodmanager.repository.FoodTypeRepository;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.ProductService;
import com.fastfoodmanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserService userService,
                           ProductService productService,
                           OrderService orderService,
                           FoodTypeRepository foodTypeRepository,
                           AllergenRepository allergenRepository) {

        return args -> {

            // FOOD TYPES
            FoodType hamburguesa = foodTypeRepository.findByName("Hamburguesa")
                    .orElseGet(() -> foodTypeRepository.save(new FoodType("Hamburguesa")));

            FoodType sides = foodTypeRepository.findByName("Sides")
                    .orElseGet(() -> foodTypeRepository.save(new FoodType("Sides")));

            // ALLERGENS
            Allergen gluten = allergenRepository.findByName("Gluten")
                    .orElseGet(() -> allergenRepository.save(new Allergen("Gluten")));

            // USERS
            if (!userService.exists("admin"))
                userService.registerUser("admin", "admin", Role.ADMIN);

            if (!userService.exists("operario1"))
                userService.registerUser("operario1", "1234", Role.OPERATOR);

            if (!userService.exists("cliente1"))
                userService.registerCustomer("cliente1", "1234");

            // PRODUCTS
            if (productService.findAll().isEmpty()) {

                Product p1 = new Product();
                p1.setName("Hamburguesa clásica");
                p1.setDescription("Ternera, lechuga, tomate, salsa");
                p1.setPrice(6.50);
                p1.setActive(true);
                p1.setType(hamburguesa);
                p1.setAllergens(List.of(gluten));
                p1.setStock(25);
                productService.save(p1);

                Product p2 = new Product();
                p2.setName("Patatas grande");
                p2.setDescription("Ración grande de patatas");
                p2.setPrice(2.90);
                p2.setActive(true);
                p2.setType(sides);
                p2.setAllergens(List.of());
                p2.setStock(40);
                productService.save(p2);
            }

            // ORDERS
            if (orderService.count() == 0) {

                User cliente = userService.findByUsername("cliente1").orElse(null);
                List<Product> productos = productService.findAll();

                if (cliente != null && productos.size() >= 2) {

                    OrderItem i1 = new OrderItem(productos.get(0), 2);
                    OrderItem i2 = new OrderItem(productos.get(1), 1);
                    Order o1 = orderService.createOrder(cliente, List.of(i1, i2));
                    o1.setAssignedTo("operario1");
                    orderService.updateStatus(o1.getId(), "EN COCINA");
                    orderService.save(o1);

                    OrderItem i3 = new OrderItem(productos.get(0), 1);
                    Order o2 = orderService.createOrder(cliente, List.of(i3));
                    o2.setAssignedTo("operario1");
                    orderService.updateStatus(o2.getId(), "PREPARANDO");
                    orderService.save(o2);
                }

                System.out.println("✅ Pedidos de ejemplo creados");
            }
        };
    }
}
