package com.fastfoodmanager;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.Order.Status;
import com.fastfoodmanager.domain.Product;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.ProductRepository;
import com.fastfoodmanager.service.OrderService;
import com.fastfoodmanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FastFoodManager {

    public static void main(String[] args) {
        SpringApplication.run(FastFoodManager.class, args);
    }

    @Bean
    CommandLineRunner seed(
            UserService userService,
            ProductRepository productRepo,
            OrderService orderService
    ) {
        return args -> {
            // 👤 Usuarios base
            if (!userService.exists("admin")) {
                userService.registerUser("admin", "1234", Role.ADMIN);
            }
            if (!userService.exists("operario1")) {
                userService.registerUser("operario1", "1234", Role.OPERATOR);
            }

            // 🍔 Productos con stock
            if (productRepo.count() == 0) {
                Product p1 = new Product();
                p1.setName("Hamburguesa Clásica");
                p1.setDescription("Ternera + queso + lechuga");
                p1.setPrice(6.50);
                p1.setAllergens("GLUTEN, LÁCTEOS");
                p1.setStock(15);
                p1.setActive(true);

                Product p2 = new Product();
                p2.setName("Patatas Deluxe");
                p2.setDescription("Gajo crujiente");
                p2.setPrice(3.00);
                p2.setAllergens("-");
                p2.setStock(30);
                p2.setActive(true);

                productRepo.save(p1);
                productRepo.save(p2);
            }

            // 📦 Pedidos de prueba
            if (orderService.count() == 0) {
                orderService.save(new Order("Juan Pérez", 12.50, Status.ENVIADO_A_COCINA, "operario1"));
                orderService.save(new Order("Ana López", 9.00, Status.PREPARANDO, "operario1"));
                System.out.println("✅ Pedidos de prueba creados para operario1");
            }
        };
    }
}
