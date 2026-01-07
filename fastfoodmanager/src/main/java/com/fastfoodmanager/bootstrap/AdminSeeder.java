package com.fastfoodmanager.bootstrap;

import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminSeeder {

    @Bean
    public CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            createIfNotExists(userService, "admin",  "admin",  Role.ADMIN, "000000000", "admin@fasttasty.com",  "Dirección admin");
            createIfNotExists(userService, "admin2", "admin2", Role.ADMIN, "000000001", "admin2@fasttasty.com", "Dirección admin2");
            createIfNotExists(userService, "admin3", "admin3", Role.ADMIN, "000000002", "admin3@fasttasty.com", "Dirección admin3");
            createIfNotExists(userService, "admin4", "admin4", Role.ADMIN, "000000003", "admin4@fasttasty.com", "Dirección admin4");
        };
    }

    private void createIfNotExists(UserService userService,
                                   String username,
                                   String password,
                                   Role role,
                                   String phone,
                                   String email,
                                   String address) {
        if (!userService.exists(username)) {
            userService.registerUser(username, password, role, phone, email, address);
        }
    }
}
