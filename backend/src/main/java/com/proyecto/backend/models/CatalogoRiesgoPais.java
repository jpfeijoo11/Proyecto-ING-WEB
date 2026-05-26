package com.proyecto.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_riesgo_pais")
public class CatalogoRiesgoPais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_puerto_o_pais", nullable = false)
    private String nombrePuertoOPais;

    /**
     * Nivel de riesgo: ALTO, MEDIO, BAJO
     */
    @Column(name = "nivel_riesgo", nullable = false)
    private String nivelRiesgo;

    /**
     * Puntos que suma al score de riesgo si la carga proviene de este origen
     */
    @Column(name = "puntos", nullable = false)
    private Integer puntos;

    @Column(name = "motivo")
    private String motivo;

    // Constructors
    public CatalogoRiesgoPais() {}

    public CatalogoRiesgoPais(String nombrePuertoOPais, String nivelRiesgo, Integer puntos, String motivo) {
        this.nombrePuertoOPais = nombrePuertoOPais;
        this.nivelRiesgo = nivelRiesgo;
        this.puntos = puntos;
        this.motivo = motivo;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombrePuertoOPais() { return nombrePuertoOPais; }
    public void setNombrePuertoOPais(String nombrePuertoOPais) { this.nombrePuertoOPais = nombrePuertoOPais; }

    public String getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }

    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
