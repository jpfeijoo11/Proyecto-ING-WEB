package com.proyecto.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    private String rol = "ADMIN";

    // --- VALIDACIÓN DE DATO SENSIBLE (Punto 1 de la rúbrica) ---
    // Nos aseguramos de que la cédula sea validada por el backend antes de tocar la BD.
    // Solo aceptará exactamente 10 dígitos numéricos.
    @NotBlank(message = "La cédula es un dato sensible obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe contener exactamente 10 números")
    @Column(unique = true, nullable = false, length = 10)
    private String cedula;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
}