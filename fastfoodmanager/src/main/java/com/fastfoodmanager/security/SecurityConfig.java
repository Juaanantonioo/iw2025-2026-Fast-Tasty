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
        // 1) RUTAS PÚBLICAS + RECURSOS ESTÁTICOS
        http.authorizeHttpRequests(auth -> auth
                // estáticos
                .requestMatchers(
                        "/images/**",
                        "/favicon.ico",
                        "/manifest.webmanifest",
                        "/sw.js",
                        "/offline-page.html",
                        "/frontend/**", "/VAADIN/**", "/webjars/**", "/themes/**"
                ).permitAll()

                // vistas públicas
                .requestMatchers("/", "/carta", "/login", "/register").permitAll()

                // vistas de OPERADOR
                .requestMatchers("/operator/**").hasRole("OPERATOR")

                // vistas de ADMIN
                .requestMatchers("/admin/**", "/products/**").hasRole("ADMIN")
        );

        // 2) Configuración base de Vaadin (deja anyRequest() como autenticado)
        super.configure(http);

        // 3) LoginView de Vaadin
        setLoginView(http, LoginView.class);

        // 4) Redirecciones tras login/logout (opcional)
        http.formLogin(form -> form.defaultSuccessUrl("/carta", true));
        http.logout(logout -> logout.logoutSuccessUrl("/login"));
    }

    // Si no usas cifrado aún, este bean no es obligatorio, pero puede quedarse.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
