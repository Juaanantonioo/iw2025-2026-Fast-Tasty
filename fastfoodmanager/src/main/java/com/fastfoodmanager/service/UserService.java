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

    public boolean authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(u -> encoder.matches(rawPassword, u.getPassword()))
                .orElse(false);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean exists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public User registerCustomer(String username, String rawPassword,
                                 String telefono, String email, String direccion) {

        if (exists(username)) throw new IllegalArgumentException("El usuario ya existe");

        User u = new User(username, encoder.encode(rawPassword), telefono, email, direccion);
        return userRepository.save(u);
    }

    public User registerUser(String username, String rawPassword, Role role,
                             String telefono, String email, String direccion) {

        if (exists(username)) throw new IllegalArgumentException("El usuario ya existe");

        User u = new User(username, encoder.encode(rawPassword), role, telefono, email, direccion);
        return userRepository.save(u);
    }

    public User registerStaff(String username, String rawPassword, Role role,
                              String telefono, String email, String direccion) {
        return registerUser(username, rawPassword, role, telefono, email, direccion);
    }

    public void registerInitialAdminIfMissing(String username, String rawPassword) {
        if (!exists(username)) {
            userRepository.save(new User(username, encoder.encode(rawPassword), Role.ADMIN, "-", "-", "-"));
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .toList();
    }

    public void updateRole(Long id, Role newRole) {
        userRepository.findById(id).ifPresent(u -> {
            u.setRole(newRole);
            userRepository.save(u);
        });
    }

    public void changeRole(Long id, Role newRole) {
        updateRole(id, newRole);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        Set<String> wanted = Stream.of(roles).collect(Collectors.toSet());

        for (GrantedAuthority ga : auth.getAuthorities()) {
            String name = ga.getAuthority(); // ROLE_ADMIN
            if (wanted.contains(name.replace("ROLE_", "")) || wanted.contains(name)) return true;
        }
        return false;
    }

    public String getCurrentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        String name = a.getName();
        return "anonymousUser".equals(name) ? null : name;
    }

    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("El usuario debe tener ID");
        }
        userRepository.save(user);
    }

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
