package com.proyecto.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "restricciones_arancelarias")
public class RestriccionArancelaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_arancelario", nullable = false, unique = true)
    private String codigoArancelario;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    /**
     * true  → requiere permiso especial (suma puntos de riesgo)
     * false → mercancía estándar sin restricciones
     */
    @Column(name = "requiere_permiso", nullable = false)
    private Boolean requierePermiso;

    @Column(name = "categoria")
    private String categoria; // Ej: ARMAMENTO, MEDICAMENTOS, QUIMICOS, ALIMENTOS

    // Constructors
    public RestriccionArancelaria() {}

    public RestriccionArancelaria(String codigoArancelario, String descripcion, Boolean requierePermiso, String categoria) {
        this.codigoArancelario = codigoArancelario;
        this.descripcion = descripcion;
        this.requierePermiso = requierePermiso;
        this.categoria = categoria;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoArancelario() { return codigoArancelario; }
    public void setCodigoArancelario(String codigoArancelario) { this.codigoArancelario = codigoArancelario; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getRequierePermiso() { return requierePermiso; }
    public void setRequierePermiso(Boolean requierePermiso) { this.requierePermiso = requierePermiso; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
