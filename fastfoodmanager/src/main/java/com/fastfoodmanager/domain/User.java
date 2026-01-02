package com.fastfoodmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // evita choque con palabra reservada "user" en H2
public class User {

    public enum Role { ADMIN, MANAGER, OPERATOR, COOK, DELIVERY, USER }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "username")
    private String username;

    @Column(nullable = false, name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role = Role.USER;

    // Nuevos campos
    @Column(nullable = false, name = "telefono")
    private String telefono;

    @Column(nullable = false, unique = true, name = "email")
    private String email;

    @Column(nullable = false, name = "direccion")
    private String direccion;

    public User() {}

    public User(String username, String password, String telefono, String email, String direccion) {
        this.username = username;
        this.password = password;
        this.role = Role.USER;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public User(String username, String password, Role role, String telefono, String email, String direccion) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    // En tu clase Usuario.java

    @Column(name = "secret_2fa") // Opcional, para definir nombre en BBDD
    private String secret2fa;

    // Añade los Getter y Setter normales
    public String getSecret2fa() { return secret2fa; }
    public void setSecret2fa(String secret2fa) { this.secret2fa = secret2fa; }
}
