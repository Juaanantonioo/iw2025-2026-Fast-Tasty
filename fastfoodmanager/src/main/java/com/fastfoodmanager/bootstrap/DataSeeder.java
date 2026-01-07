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

        };
    }
}
