package com.fastfoodmanager.bootstrap;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner initAdmin(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin")); // BCrypt
                admin.setRole(Role.ADMIN);

                // Campos obligatorios (NOT NULL) en la tabla users
                admin.setEmail("admin@fasttasty.local");
                admin.setTelefono("000000000");
                admin.setDireccion("N/A");

                repo.save(admin);
            }
        };
    }
}
