package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.UserRepository;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    // ✔ Autenticación comparando con BCrypt
    public boolean authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(u -> encoder.matches(rawPassword, u.getPassword()))
                .orElse(false);
    }

    // ✔ Buscar usuario por nombre
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ✔ Comprobar existencia
    public boolean exists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // ✔ Registro público: siempre rol USER
    public User registerCustomer(String username, String rawPassword,
                                 String telefono, String email, String direccion) {

        if (exists(username)) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        User u = new User(
                username,
                encoder.encode(rawPassword),
                telefono,
                email,
                direccion
        );

        return userRepository.save(u);
    }

    // ✔ Registrar usuario con rol específico (ADMIN, OPERATOR...)
    public User registerUser(String username, String rawPassword, Role role,
                             String telefono, String email, String direccion) {

        if (exists(username)) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        User u = new User(
                username,
                encoder.encode(rawPassword),
                role,
                telefono,
                email,
                direccion
        );

        return userRepository.save(u);
    }

    // ✔ Usado al iniciar la app para crear admin si no existe
    public void registerUser(String username, String rawPassword) {
        if (!exists(username)) {
            userRepository.save(
                    new User(username, encoder.encode(rawPassword), Role.ADMIN, "-", "-", "-")
            );
        }
    }

    // ✔ Listar todos
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ✔ Cambiar rol
    public void updateRole(Long id, Role newRole) {
        userRepository.findById(id).ifPresent(u -> {
            u.setRole(newRole);
            userRepository.save(u);
        });
    }

    public void changeRole(Long id, Role newRole) {
        updateRole(id, newRole);
    }

    // ✔ Eliminar
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ✔ Comprobar roles actuales
    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        Set<String> wanted = Stream.of(roles).collect(Collectors.toSet());

        for (GrantedAuthority ga : auth.getAuthorities()) {
            String name = ga.getAuthority(); // ejemplo: ROLE_ADMIN
            if (wanted.contains(name.replace("ROLE_", "")) || wanted.contains(name)) {
                return true;
            }
        }
        return false;
    }

    // ✔ Obtener username actual
    public String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        String name = a.getName();
        return "anonymousUser".equals(name) ? null : name;
    }

    // ✔ Cerrar sesión
    public void logout() {
        SecurityContextHolder.clearContext();
        try {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                session.getSession().invalidate();
                session.close();
            }
        } catch (Exception ignored) {}
    }
}
