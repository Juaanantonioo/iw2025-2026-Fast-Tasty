package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.User;
import com.fastfoodmanager.domain.User.Role;
import com.fastfoodmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Autenticación simple (sin encriptar)
    public boolean authenticate(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.isPresent() && userOpt.get().getPassword().equals(rawPassword);
    }

    // ✅ Buscar usuario por nombre
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // ✅ Comprobar existencia
    public boolean exists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // ✅ Registro público: SIEMPRE rol USER
    public User registerCustomer(String username, String rawPassword) {
        if (exists(username)) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(rawPassword);
        u.setRole(Role.USER); // 🔒 siempre USER
        return userRepository.save(u);
    }

    // ✅ Registrar usuario con rol específico (para admin)
    public User registerUser(String username, String rawPassword, Role role) {
        if (exists(username)) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        User u = new User(username, rawPassword, role);
        return userRepository.save(u);
    }

    // ✅ Método usado en el seed (crear admin inicial si no existe)
    public void registerUser(String username, String rawPassword) {
        if (!exists(username)) {
            userRepository.save(new User(username, rawPassword, Role.ADMIN));
        }
    }

    // ✅ Listar todos
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ✅ Cambiar rol (nombre “oficial”)
    public void updateRole(Long id, Role newRole) {
        userRepository.findById(id).ifPresent(u -> {
            u.setRole(newRole);
            userRepository.save(u);
        });
    }

    // ✅ Alias para compatibilidad con tu vista (llama a updateRole)
    public void changeRole(Long id, Role newRole) {
        updateRole(id, newRole);
    }

    // ✅ Eliminar
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
