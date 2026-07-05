package com.proyecto.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo esperado por {@code PUT /api/operaciones/{id}/estado}.
 *
 * <p>Reemplaza el contrato anterior, que recibía un {@code String} JSON
 * crudo (por ejemplo {@code "AFORO"}) y requería limpiar manualmente las
 * comillas en el controller. Un objeto JSON explícito es más robusto,
 * autodescriptivo y validable con Bean Validation.</p>
 */
public class CambioEstadoRequest {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    public CambioEstadoRequest() {}

    public CambioEstadoRequest(String estado) {
        this.estado = estado;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
