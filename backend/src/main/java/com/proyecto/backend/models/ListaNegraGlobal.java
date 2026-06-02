package com.proyecto.backend.models;

import jakarta.persistence.*;

/**
 * Entidades sancionadas internacionalmente (OFAC, ONU, INTERPOL).
 * Se compara contra importador_historial para detectar si una empresa
 * registrada en el sistema está también en una lista de sancionados global.
 */
@Entity
@Table(name = "lista_negra_global")
public class ListaNegraGlobal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruc_sancionado", nullable = false, unique = true)
    private String rucSancionado;

    @Column(name = "nombre_entidad", nullable = false)
    private String nombreEntidad;

    /**
     * Organismo que emitió la sanción: OFAC, ONU, INTERPOL, UE, etc.
     */
    @Column(name = "organismo_sancionador", nullable = false)
    private String organismoSancionador;

    /**
     * Motivo de la sanción: NARCOTRAFICO, LAVADO_ACTIVOS, TERRORISMO, etc.
     */
    @Column(name = "motivo_sancion", nullable = false)
    private String motivoSancion;

    /**
     * Puntos adicionales que suma al score de riesgo si el importador
     * coincide con esta entrada. Va sobre el score normal del importador.
     */
    @Column(name = "puntos_extra", nullable = false)
    private Integer puntosExtra;

    // Constructors
    public ListaNegraGlobal() {}

    public ListaNegraGlobal(String rucSancionado, String nombreEntidad,
                             String organismoSancionador, String motivoSancion, Integer puntosExtra) {
        this.rucSancionado = rucSancionado;
        this.nombreEntidad = nombreEntidad;
        this.organismoSancionador = organismoSancionador;
        this.motivoSancion = motivoSancion;
        this.puntosExtra = puntosExtra;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRucSancionado() { return rucSancionado; }
    public void setRucSancionado(String rucSancionado) { this.rucSancionado = rucSancionado; }

    public String getNombreEntidad() { return nombreEntidad; }
    public void setNombreEntidad(String nombreEntidad) { this.nombreEntidad = nombreEntidad; }

    public String getOrganismoSancionador() { return organismoSancionador; }
    public void setOrganismoSancionador(String organismoSancionador) { this.organismoSancionador = organismoSancionador; }

    public String getMotivuSancion() { return motivoSancion; }
    public void setMotivuSancion(String motivoSancion) { this.motivoSancion = motivoSancion; }

    public Integer getPuntosExtra() { return puntosExtra; }
    public void setPuntosExtra(Integer puntosExtra) { this.puntosExtra = puntosExtra; }
}
