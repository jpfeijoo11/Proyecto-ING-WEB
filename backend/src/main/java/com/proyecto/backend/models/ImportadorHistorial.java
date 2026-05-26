package com.proyecto.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "importador_historial")
public class ImportadorHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_empresa", nullable = false)
    private String nombreEmpresa;

    @Column(name = "ruc_empresa", nullable = false, unique = true)
    private String rucEmpresa;

    @Column(name = "infracciones_previas", nullable = false)
    private Integer infraccionesPrevias = 0;

    @Column(name = "pais_origen")
    private String paisOrigen;

    // Constructors
    public ImportadorHistorial() {}

    public ImportadorHistorial(String nombreEmpresa, String rucEmpresa, Integer infraccionesPrevias, String paisOrigen) {
        this.nombreEmpresa = nombreEmpresa;
        this.rucEmpresa = rucEmpresa;
        this.infraccionesPrevias = infraccionesPrevias;
        this.paisOrigen = paisOrigen;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getRucEmpresa() { return rucEmpresa; }
    public void setRucEmpresa(String rucEmpresa) { this.rucEmpresa = rucEmpresa; }

    public Integer getInfraccionesPrevias() { return infraccionesPrevias; }
    public void setInfraccionesPrevias(Integer infraccionesPrevias) { this.infraccionesPrevias = infraccionesPrevias; }

    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }
}
