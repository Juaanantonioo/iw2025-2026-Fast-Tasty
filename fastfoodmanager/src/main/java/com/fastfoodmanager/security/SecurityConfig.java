package com.fastfoodmanager.security;

import com.fastfoodmanager.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // 👈 Importante
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // 👈 Importante
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/images/**",
                        "/favicon.ico",
                        "/manifest.webmanifest",
                        "/sw.js",
                        "/offline-page.html",
                        "/frontend/**", "/VAADIN/**", "/webjars/**", "/themes/**"
                ).permitAll()

                .requestMatchers("/", "/carta", "/login", "/register").permitAll()

                .requestMatchers("/manager/**").hasRole("MANAGER")
                .requestMatchers("/cook/**").hasRole("COOK")
                .requestMatchers("/delivery/**").hasRole("DELIVERY")
                .requestMatchers("/operator/**").hasRole("OPERATOR")
                .requestMatchers("/admin/**", "/products/**").hasRole("ADMIN")
                .requestMatchers("/profile").authenticated()
        );

        super.configure(http);
        setLoginView(http, LoginView.class);

        // NOTA: Al hacer login manual en LoginView, este successHandler
        // podría no ejecutarse automáticamente. Ver explicación abajo.
        http.formLogin(form -> form.successHandler(this::onLoginSuccess));
        http.logout(logout -> logout.logoutSuccessUrl("/login"));
    }

    // 👇 ESTE ES EL BEAN QUE TE FALTABA Y CAUSABA EL ERROR
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    private void onLoginSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        boolean isOperator = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERATOR"));
        boolean isCook = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COOK"));
        boolean isDelivery = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DELIVERY"));

        if (isAdmin) {
            response.sendRedirect("/admin/users");
            return;
        }
        if (isManager) {
            response.sendRedirect("/manager/users");
            return;
        }
        if (isOperator) {
            response.sendRedirect("/operator/orders");
            return;
        }
        if (isCook) {
            response.sendRedirect("/cook/orders");
            return;
        }
        if (isDelivery) {
            response.sendRedirect("/delivery/orders");
            return;
        }

        response.sendRedirect("/carta"); // USER normal
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}