package com.proyecto.backend.services;

import com.proyecto.backend.dto.DetalleRiesgoDTO;
import com.proyecto.backend.dto.OperacionConRiesgoResponse;
import com.proyecto.backend.models.OperacionAduanera;
import com.proyecto.backend.repositories.OperacionAduaneraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Orquesta el ciclo de vida de una {@link OperacionAduanera}.
 *
 * <p>Antes esta orquestación (fijar el estado inicial, invocar el motor de
 * riesgo y persistir) vivía directamente en {@code OperacionAduaneraController},
 * mezclando responsabilidades HTTP con lógica de negocio (violación de SRP).
 * Con este servicio, el controller queda como una capa delgada que solo
 * traduce peticiones HTTP a llamadas de este servicio.</p>
 */
@Service
public class OperacionAduaneraService {

    /** Estados válidos que puede recibir el endpoint de cambio de estado. */
    private static final List<String> ESTADOS_VALIDOS = List.of(
            "DOCUMENTACION", "LOGISTICA", "EN_TRANSITO", "LLEGADA_PUERTO",
            "DESADUANIZACION", "CERRADO", "AFORO", "LIBERADA"
    );

    private final OperacionAduaneraRepository operacionRepository;
    private final PerfilRiesgoService perfilRiesgoService;

    public OperacionAduaneraService(OperacionAduaneraRepository operacionRepository,
                                     PerfilRiesgoService perfilRiesgoService) {
        this.operacionRepository = operacionRepository;
        this.perfilRiesgoService = perfilRiesgoService;
    }

    /** Registra una nueva operación: fija su estado inicial, ejecuta el motor de riesgo y la persiste. */
    public OperacionConRiesgoResponse registrarOperacion(OperacionAduanera operacion) {
        operacion.setEstado("DOCUMENTACION");
        DetalleRiesgoDTO detalle = perfilRiesgoService.evaluarYObtenerDetalle(operacion);
        OperacionAduanera guardada = operacionRepository.save(operacion);
        return new OperacionConRiesgoResponse(guardada, detalle);
    }

    /**
     * Cambia el estado de una operación existente, validando que el nuevo
     * estado sea uno de los estados reconocidos del flujo. Antes, el
     * controller aceptaba cualquier texto libre (incluyendo comillas que
     * había que limpiar a mano) sin ninguna validación.
     *
     * @throws IllegalArgumentException si el estado no es válido
     */
    public Optional<OperacionAduanera> cambiarEstado(Long id, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException("El estado no puede estar vacío.");
        }
        String estadoNormalizado = nuevoEstado.trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new IllegalArgumentException(
                    "Estado '" + nuevoEstado + "' no reconocido. Valores válidos: " + ESTADOS_VALIDOS);
        }
        return operacionRepository.findById(id).map(operacion -> {
            operacion.setEstado(estadoNormalizado);
            return operacionRepository.save(operacion);
        });
    }
}
