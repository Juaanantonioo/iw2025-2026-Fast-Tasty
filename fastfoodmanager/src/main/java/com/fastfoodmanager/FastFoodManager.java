package com.fastfoodmanager;

import com.fastfoodmanager.domain.User.Role;
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
    CommandLineRunner seed(UserService userService) {
        return args -> {
            if (!userService.exists("admin")) {
                userService.registerUser("admin", "1234", Role.ADMIN);
                userService.registerUser("operario1", "1234", Role.OPERATOR);
                System.out.println("✅ Semilla creada: admin/1234 (ADMIN), operario1/1234 (OPERATOR)");
            } else {
                System.out.println("ℹ️ Usuarios base ya existen");
            }
        };
    }
}
