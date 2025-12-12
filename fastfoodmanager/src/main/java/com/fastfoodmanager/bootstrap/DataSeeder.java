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
import java.util.HashSet;
import java.util.Set;
import java.io.InputStream;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserService userService,
                           ProductService productService,
                           OrderService orderService,
                           FoodTypeRepository foodTypeRepository,
                           AllergenRepository allergenRepository) {

        return args -> {

            FoodType hamburguesa = foodTypeRepository.findByName("Hamburguesa")
                    .orElseGet(() -> foodTypeRepository.save(new FoodType("Hamburguesa")));

            FoodType sides = foodTypeRepository.findByName("Sides")
                    .orElseGet(() -> foodTypeRepository.save(new FoodType("Sides")));

            Allergen gluten = allergenRepository.findByName("Gluten")
                    .orElseGet(() -> allergenRepository.save(new Allergen("Gluten")));

            if (!userService.exists("admin"))
                userService.registerUser("admin", "admin", Role.ADMIN, "937567321", "admin@gmail.com", "Calle Mela, 3, Cádiz");

            if (!userService.exists("encargado1"))
                userService.registerUser("encargado1", "1234", Role.MANAGER, "937500001", "encargado1@gmail.com", "Calle Encargado, 1, Cádiz");

            if (!userService.exists("operario1"))
                userService.registerUser("operario1", "1234", Role.OPERATOR, "937573321", "operario1@gmail.com", "Calle Melo, 2, De pan");

            if (!userService.exists("cocinero1"))
                userService.registerUser("cocinero1", "1234", Role.COOK, "937500002", "cocinero1@gmail.com", "Cocina 1");

            if (!userService.exists("repartidor1"))
                userService.registerUser("repartidor1", "1234", Role.DELIVERY, "937500003", "repartidor1@gmail.com", "Reparto 1");

            if (!userService.exists("cliente1"))
                userService.registerCustomer("cliente1", "1234", "937572051", "cliente1@gmail.com", "Calle Milo, 5, Abanca");

            if (productService.findAll().isEmpty()) {

                Product p1 = new Product();
                p1.setName("Hamburguesa clásica");
                p1.setDescription("Ternera, lechuga, tomate, salsa");
                p1.setPrice(6.50);
                p1.setActive(true);
                p1.setType(hamburguesa);
                p1.setAllergens(new HashSet<>(Set.of(gluten)));
                p1.setStock(25);

                try (InputStream is = getClass().getResourceAsStream("/images/clasica.jpg")) {
                    if (is != null) p1.setImage(is.readAllBytes());
                }
                productService.save(p1);

                Product p2 = new Product();
                p2.setName("Patatas grande");
                p2.setDescription("Ración grande de patatas");
                p2.setPrice(2.90);
                p2.setActive(true);
                p2.setType(sides);
                p2.setAllergens(new HashSet<>());
                p2.setStock(40);

                try (InputStream is = getClass().getResourceAsStream("/images/patatas_con_queso_y_bacon.jpg")) {
                    if (is != null) p2.setImage(is.readAllBytes());
                }
                productService.save(p2);
            }

            // Pedidos de ejemplo
            if (orderService.count() == 0) {

                User cliente = userService.findByUsername("cliente1").orElse(null);
                List<Product> productos = productService.findAll();

                if (cliente != null && productos.size() >= 2) {

                    // Pedido 1: ENVIADO (recién pagado)
                    OrderItem i1 = new OrderItem(productos.get(0), 2);
                    OrderItem i2 = new OrderItem(productos.get(1), 1);
                    Order o1 = orderService.createOrder(cliente, List.of(i1, i2));
                    orderService.markAsPaid(o1.getId());

                    // Pedido 2: ya en cocina (simulado: operario lo manda a cocina)
                    OrderItem i3 = new OrderItem(productos.get(0), 1);
                    Order o2 = orderService.createOrder(cliente, List.of(i3));
                    orderService.markAsPaid(o2.getId());
                    orderService.sendToKitchen(o2.getId(), "operario1");

                    System.out.println("✅ Pedidos de ejemplo creados");
                }
            }
        };
    }
}
