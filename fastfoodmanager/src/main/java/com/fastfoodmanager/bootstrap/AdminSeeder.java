package com.fastfoodmanager.bootstrap;

import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            if (!userService.exists("admin")) {
                userService.registerUser(
                        "admin",
                        "admin",
                        Role.ADMIN,
                        "000000000",
                        "admin@fasttasty.com",
                        "Dirección admin"
                );
            }
        };
    }
}
