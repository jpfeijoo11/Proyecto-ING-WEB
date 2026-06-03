package com.proyecto.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operaciones_aduaneras")
public class OperacionAduanera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_tracking", nullable = false, unique = true)
    private String numeroTracking;

    @Column(name = "tipo_operacion", nullable = false)
    private String tipoOperacion; // IMPORTACION, EXPORTACION

    @Column(nullable = false)
    private String estado; // DOCUMENTACION, LOGISTICA, EN_TRANSITO, LLEGADA_PUERTO, DESADUANIZACION, CERRADO

    @Column(name = "puerto_origen")
    private String puertoOrigen;

    @Column(name = "estado_registros")
    private String estadoRegistros;

    @Column(name = "puerto_destino")
    private String puertoDestino;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // ─── CAMPOS PARA EL MOTOR DE PERFILAMIENTO DE RIESGO ────────────────────

    /** FK lógica al ImportadorHistorial */
    @Column(name = "id_importador")
    private Long idImportador;

    /** Código del arancel de la mercancía (Ej: "2933.59", "9301.10") */
    @Column(name = "codigo_arancelario")
    private String codigoArancelario;

    /**
     * Canal de aforo asignado automáticamente:
     * VERDE (0-30 pts) | AMARILLO (31-70 pts) | ROJO (71-100 pts)
     */
    @Column(name = "canal_aforo")
    private String canalAforo;

    /** Puntaje de riesgo calculado por el PerfilRiesgoService */
    @Column(name = "puntaje_riesgo")
    private Integer puntajeRiesgo;

    // ─── CONSTRUCTORS ────────────────────────────────────────────────────────

    public OperacionAduanera() {
        this.fechaRegistro = LocalDateTime.now();
    }

    // ─── GETTERS & SETTERS ───────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroTracking() { return numeroTracking; }
    public void setNumeroTracking(String numeroTracking) { this.numeroTracking = numeroTracking; }

    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPuertoOrigen() { return puertoOrigen; }
    public void setPuertoOrigen(String puertoOrigen) { this.puertoOrigen = puertoOrigen; }

    public String getPuertoDestino() { return puertoDestino; }
    public void setPuertoDestino(String puertoDestino) { this.puertoDestino = puertoDestino; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Long getIdImportador() { return idImportador; }
    public void setIdImportador(Long idImportador) { this.idImportador = idImportador; }

    public String getCodigoArancelario() { return codigoArancelario; }
    public void setCodigoArancelario(String codigoArancelario) { this.codigoArancelario = codigoArancelario; }

    public String getCanalAforo() { return canalAforo; }
    public void setCanalAforo(String canalAforo) { this.canalAforo = canalAforo; }

    public Integer getPuntajeRiesgo() { return puntajeRiesgo; }
    public void setPuntajeRiesgo(Integer puntajeRiesgo) { this.puntajeRiesgo = puntajeRiesgo; }

    public String getEstadoRegistros() { return estadoRegistros; }
    public void setEstadoRegistros(String estadoRegistros) { this.estadoRegistros = estadoRegistros; }
}
