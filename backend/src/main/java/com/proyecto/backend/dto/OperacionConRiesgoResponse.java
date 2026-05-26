package com.proyecto.backend.dto;

import com.proyecto.backend.models.OperacionAduanera;

/**
 * Respuesta completa del POST /api/operaciones.
 * Incluye la operación guardada y el desglose detallado
 * del análisis de riesgo obtenido mediante JOIN en la BD.
 */
public class OperacionConRiesgoResponse {

    private OperacionAduanera operacion;
    private DetalleRiesgoDTO detalleRiesgo;

    public OperacionConRiesgoResponse() {}

    public OperacionConRiesgoResponse(OperacionAduanera operacion, DetalleRiesgoDTO detalleRiesgo) {
        this.operacion = operacion;
        this.detalleRiesgo = detalleRiesgo;
    }

    public OperacionAduanera getOperacion() { return operacion; }
    public void setOperacion(OperacionAduanera operacion) { this.operacion = operacion; }

    public DetalleRiesgoDTO getDetalleRiesgo() { return detalleRiesgo; }
    public void setDetalleRiesgo(DetalleRiesgoDTO detalleRiesgo) { this.detalleRiesgo = detalleRiesgo; }
}
