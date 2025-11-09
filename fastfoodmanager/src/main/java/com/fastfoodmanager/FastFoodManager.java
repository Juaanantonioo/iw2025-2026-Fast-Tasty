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

import java.util.List;

@SpringBootApplication
public class FastFoodManager {

    public static void main(String[] args) {
        SpringApplication.run(FastFoodManager.class, args);
    }

    @Bean
    CommandLineRunner seed(UserService userService,
                           ProductService productService,
                           OrderService orderService) {
        return args -> {
            // ───────────────────────
            // Usuarios base
            // ───────────────────────
            if (!userService.exists("admin")) {
                userService.registerUser("admin", "admin", Role.ADMIN); // admin/admin
            }
            if (!userService.exists("operario1")) {
                userService.registerUser("operario1", "1234", Role.OPERATOR);
            }
            if (!userService.exists("cliente1")) {
                userService.registerCustomer("cliente1", "1234"); // rol USER
            }

            // ───────────────────────
            // Productos de ejemplo
            // ───────────────────────
            if (productService.findAll().isEmpty()) {
                Product p1 = new Product();
                p1.setName("Hamburguesa clásica");
                p1.setDescription("Ternera, lechuga, tomate, salsa");
                p1.setPrice(6.50);
                p1.setActive(true);
                p1.setAllergens("gluten");
                p1.setStock(25);
                productService.save(p1);

                Product p2 = new Product();
                p2.setName("Patatas grande");
                p2.setDescription("Ración grande de patatas");
                p2.setPrice(2.90);
                p2.setActive(true);
                p2.setAllergens("");
                p2.setStock(40);
                productService.save(p2);
            }

            // ───────────────────────
            // Pedidos de ejemplo (si no hay)
            // ───────────────────────
            if (orderService.count() == 0) {
                User cliente = userService.findByUsername("cliente1").orElse(null);
                List<Product> productos = productService.findAll();

                if (cliente != null && productos.size() >= 2) {
                    // Pedido 1
                    OrderItem i1 = new OrderItem(productos.get(0), 2);
                    OrderItem i2 = new OrderItem(productos.get(1), 1);
                    Order o1 = orderService.createOrder(cliente, List.of(i1, i2));
                    o1.setAssignedTo("operario1");                  // asignado al operario
                    orderService.updateStatus(o1.getId(), "EN COCINA");
                    orderService.save(o1);                           // guarda la asignación

                    // Pedido 2
                    OrderItem i3 = new OrderItem(productos.get(0), 1);
                    Order o2 = orderService.createOrder(cliente, List.of(i3));
                    o2.setAssignedTo("operario1");                  // asignado al operario
                    orderService.updateStatus(o2.getId(), "PREPARANDO");
                    orderService.save(o2);                           // guarda la asignación
                }

                System.out.println("✅ Pedidos de ejemplo creados (asignados a operario1)");
            } else {
                System.out.println("ℹ️ Usuarios/productos/pedidos ya presentes");
            }
        };
    }
}
