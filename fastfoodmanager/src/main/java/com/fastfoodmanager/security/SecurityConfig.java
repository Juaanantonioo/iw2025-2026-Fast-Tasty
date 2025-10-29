package com.fastfoodmanager.security;

import com.fastfoodmanager.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 1) Abre tus recursos estáticos propios ANTES del super
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/images/**",
                        "/favicon.ico",
                        "/manifest.webmanifest",
                        "/sw.js",
                        "/offline-page.html"
                ).permitAll()
        );

        // 2) Configuración por defecto de Vaadin (incluye anyRequest)
        super.configure(http);

        // 3) Tu login de Vaadin
        setLoginView(http, LoginView.class);

        // 4) (Opcional) redirecciones
        http.formLogin(form -> form.defaultSuccessUrl("/carta", true));
        http.logout(logout -> logout.logoutSuccessUrl("/login"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
