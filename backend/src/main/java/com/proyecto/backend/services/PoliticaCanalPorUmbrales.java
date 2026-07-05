package com.proyecto.backend.services;

import org.springframework.stereotype.Component;

/**
 * Implementación por defecto (patrón Strategy) de {@link PoliticaCanalAforo}:
 * clasifica el canal de aforo mediante dos umbrales fijos sobre el puntaje
 * total de riesgo. Es la única implementación registrada hoy en Spring,
 * pero el resto del sistema (PerfilRiesgoService) solo conoce la interfaz,
 * por lo que agregar una política alternativa (p. ej. umbrales configurables
 * desde base de datos) no requiere modificar el motor de riesgo.
 */
@Component
public class PoliticaCanalPorUmbrales implements PoliticaCanalAforo {

    private static final int UMBRAL_VERDE = 30;
    private static final int UMBRAL_AMARILLO = 70;

    @Override
    public String resolverCanal(int puntajeTotal) {
        if (puntajeTotal <= UMBRAL_VERDE) return "VERDE";
        if (puntajeTotal <= UMBRAL_AMARILLO) return "AMARILLO";
        return "ROJO";
    }

    @Override
    public String descripcionCanal(String canal) {
        return switch (canal) {
            case "VERDE" -> "Desaduanización Automática: carga aprobada sin inspección adicional.";
            case "AMARILLO" -> "Aforo Documental: el Agente debe subir Factura, Certificado de Origen y Póliza de Seguro.";
            case "ROJO" -> "Aforo Físico Intrusivo: el Inspector debe abrir el contenedor presencialmente y llenar el reporte de hallazgos.";
            default -> "";
        };
    }
}
