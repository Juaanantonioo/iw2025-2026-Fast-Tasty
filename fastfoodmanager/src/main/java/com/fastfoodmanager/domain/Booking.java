package com.fastfoodmanager.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Booking {
    private String nombre;
    private String telefono;
    private String email;
    private LocalDate fecha;
    private LocalTime hora;
    private Integer personas;
    private String comentarios;

    // Constructor
    public Booking(String nombre, String telefono, String email, LocalDate fecha,
                   LocalTime hora, Integer personas, String comentarios) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.fecha = fecha;
        this.hora = hora;
        this.personas = personas;
        this.comentarios = comentarios;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public Integer getPersonas() { return personas; }
    public void setPersonas(Integer personas) { this.personas = personas; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
}