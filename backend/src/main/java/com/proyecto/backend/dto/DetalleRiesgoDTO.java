package com.proyecto.backend.dto;

/**
 * Resultado del análisis cruzado de riesgo aduanero.
 * Cada campo indica qué encontró el JOIN con cada tabla de catálogo
 * y cuántos puntos aportó al puntaje final.
 */
public class DetalleRiesgoDTO {

    // ── VECTOR 1: Cruce con catalogo_riesgo_pais ─────────────────────────────
    /** Nivel de riesgo encontrado en el catálogo (ALTO / MEDIO / BAJO / N/A) */
    private String nivelRiesgoPais;
    /** Puntos sumados por el origen (0, 15 o 30 según nivel) */
    private int puntosOrigen;
    /** Motivo registrado en el catálogo para ese puerto */
    private String motivoPais;
    /** Indica si el puerto/país de origen estuvo en el catálogo */
    private boolean origenEnCatalogo;

    // ── VECTOR 2: Cruce con importador_historial ──────────────────────────────
    /** Nombre de la empresa importadora encontrada en historial */
    private String nombreImportador;
    /** Cantidad de infracciones previas registradas */
    private int infraccionesImportador;
    /** Puntos sumados por historial del importador (0 o 40) */
    private int puntosImportador;
    /** Indica si el importador tenía más de 2 infracciones (umbral de riesgo) */
    private boolean importadorInfractor;

    // ── VECTOR 3: Cruce con restricciones_arancelarias ────────────────────────
    /** Descripción del producto según el catálogo arancelario */
    private String descripcionMercancia;
    /** Categoría del producto (ARMAMENTO, QUIMICOS, MEDICAMENTOS, etc.) */
    private String categoriaMercancia;
    /** true si la mercancía requiere permiso especial de importación */
    private boolean requierePermiso;
    /** Puntos sumados por la restricción arancelaria (0 o 30) */
    private int puntosMercancia;

    // ── VECTOR 4: Cruce importador_historial vs lista_negra_global ───────────
    /** true si el RUC del importador aparece en la lista negra global */
    private boolean importadorEnListaNegra;
    /** Organismo que sancionó al importador (OFAC, ONU, INTERPOL, UE) */
    private String organismoSancionador;
    /** Motivo de la sanción internacional */
    private String motivoSancion;
    /** Puntos extra por estar en lista negra (0 o 50) */
    private int puntosListaNegra;

    // ── RESULTADO FINAL ───────────────────────────────────────────────────────
    /** Suma total: puntosOrigen + puntosImportador + puntosMercancia + puntosListaNegra */
    private int puntajeTotal;
    /** Canal asignado: VERDE | AMARILLO | ROJO */
    private String canalAforo;
    /** Descripción de lo que implica el canal asignado */
    private String descripcionCanal;

    // Getters & Setters
    public String getNivelRiesgoPais() { return nivelRiesgoPais; }
    public void setNivelRiesgoPais(String nivelRiesgoPais) { this.nivelRiesgoPais = nivelRiesgoPais; }

    public int getPuntosOrigen() { return puntosOrigen; }
    public void setPuntosOrigen(int puntosOrigen) { this.puntosOrigen = puntosOrigen; }

    public String getMotivoPais() { return motivoPais; }
    public void setMotivoPais(String motivoPais) { this.motivoPais = motivoPais; }

    public boolean isOrigenEnCatalogo() { return origenEnCatalogo; }
    public void setOrigenEnCatalogo(boolean origenEnCatalogo) { this.origenEnCatalogo = origenEnCatalogo; }

    public String getNombreImportador() { return nombreImportador; }
    public void setNombreImportador(String nombreImportador) { this.nombreImportador = nombreImportador; }

    public int getInfraccionesImportador() { return infraccionesImportador; }
    public void setInfraccionesImportador(int infraccionesImportador) { this.infraccionesImportador = infraccionesImportador; }

    public int getPuntosImportador() { return puntosImportador; }
    public void setPuntosImportador(int puntosImportador) { this.puntosImportador = puntosImportador; }

    public boolean isImportadorInfractor() { return importadorInfractor; }
    public void setImportadorInfractor(boolean importadorInfractor) { this.importadorInfractor = importadorInfractor; }

    public String getDescripcionMercancia() { return descripcionMercancia; }
    public void setDescripcionMercancia(String descripcionMercancia) { this.descripcionMercancia = descripcionMercancia; }

    public String getCategoriaMercancia() { return categoriaMercancia; }
    public void setCategoriaMercancia(String categoriaMercancia) { this.categoriaMercancia = categoriaMercancia; }

    public boolean isRequierePermiso() { return requierePermiso; }
    public void setRequierePermiso(boolean requierePermiso) { this.requierePermiso = requierePermiso; }

    public int getPuntosMercancia() { return puntosMercancia; }
    public void setPuntosMercancia(int puntosMercancia) { this.puntosMercancia = puntosMercancia; }

    public int getPuntajeTotal() { return puntajeTotal; }
    public void setPuntajeTotal(int puntajeTotal) { this.puntajeTotal = puntajeTotal; }

    public String getCanalAforo() { return canalAforo; }
    public void setCanalAforo(String canalAforo) { this.canalAforo = canalAforo; }

    public String getDescripcionCanal() { return descripcionCanal; }
    public void setDescripcionCanal(String descripcionCanal) { this.descripcionCanal = descripcionCanal; }

    public boolean isImportadorEnListaNegra() { return importadorEnListaNegra; }
    public void setImportadorEnListaNegra(boolean importadorEnListaNegra) { this.importadorEnListaNegra = importadorEnListaNegra; }

    public String getOrganismoSancionador() { return organismoSancionador; }
    public void setOrganismoSancionador(String organismoSancionador) { this.organismoSancionador = organismoSancionador; }

    public String getMotivoSancion() { return motivoSancion; }
    public void setMotivoSancion(String motivoSancion) { this.motivoSancion = motivoSancion; }

    public int getPuntosListaNegra() { return puntosListaNegra; }
    public void setPuntosListaNegra(int puntosListaNegra) { this.puntosListaNegra = puntosListaNegra; }
}
