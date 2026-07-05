package com.proyecto.backend.controllers;

import com.proyecto.backend.dto.CambioEstadoRequest;
import com.proyecto.backend.dto.DetalleRiesgoDTO;
import com.proyecto.backend.dto.OperacionConRiesgoResponse;
import com.proyecto.backend.models.OperacionAduanera;
import com.proyecto.backend.repositories.OperacionAduaneraRepository;
import com.proyecto.backend.services.OperacionAduaneraService;
import com.proyecto.backend.services.PerfilRiesgoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operaciones")
@CrossOrigin(origins = "http://localhost:4200")
public class OperacionAduaneraController {

    @Autowired
    private OperacionAduaneraRepository operacionRepository;

    @Autowired
    private PerfilRiesgoService perfilRiesgoService;

    @Autowired
    private OperacionAduaneraService operacionAduaneraService;

    // ── GET todas las operaciones ─────────────────────────────────────────────
    @GetMapping
    public List<OperacionAduanera> obtenerTodas() {
        return operacionRepository.findAll();
    }

    /**
     * POST /api/operaciones
     *
     * El controller solo traduce la petición HTTP; toda la orquestación
     * (fijar estado inicial, ejecutar el motor de riesgo y persistir) vive
     * en {@link OperacionAduaneraService#registrarOperacion}.
     */
    @PostMapping
    public ResponseEntity<OperacionConRiesgoResponse> crearOperacion(
            @RequestBody OperacionAduanera operacion) {
        return ResponseEntity.ok(operacionAduaneraService.registrarOperacion(operacion));
    }

    // ── GET por tracking ──────────────────────────────────────────────────────
    @GetMapping("/tracking/{numeroTracking}")
    public ResponseEntity<OperacionAduanera> obtenerPorTracking(
            @PathVariable String numeroTracking) {
        return operacionRepository.findByNumeroTracking(numeroTracking)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/operaciones/{id}/estado
     *
     * Antes recibía un {@code String} JSON crudo (había que limpiar
     * comillas a mano) y no validaba el valor recibido. Ahora recibe un
     * DTO explícito ({@link CambioEstadoRequest}) y el servicio valida que
     * el estado sea uno de los reconocidos por el flujo antes de guardarlo.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequest request) {
        try {
            return operacionAduaneraService.cambiarEstado(id, request.getEstado())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{id}/analisis")
    public ResponseEntity<OperacionConRiesgoResponse> obtenerAnalisis(@PathVariable Long id) {
        return operacionRepository.findById(id)
                .map(operacion -> {
                    DetalleRiesgoDTO detalle = perfilRiesgoService.analizarOperacionExistente(operacion);
                    return ResponseEntity.ok(new OperacionConRiesgoResponse(operacion, detalle));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET alertas Canal ROJO (dashboard del Inspector) ─────────────────────
    @GetMapping("/alerta-roja")
    public List<OperacionAduanera> obtenerAlertasRojas() {
        return operacionRepository.findAll().stream()
                .filter(op -> "ROJO".equals(op.getCanalAforo()))
                .filter(op -> !"CERRADO".equals(op.getEstado()))
                .toList();
    }

    /**
     * DELETE /api/operaciones/{id}
     * Elimina una operación aduanera por ID. Solo el Admin debe invocar este endpoint.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarOperacion(@PathVariable Long id) {
        if (!operacionRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Operación no encontrada.");
        }
        operacionRepository.deleteById(id);
        return ResponseEntity.ok("Operación eliminada correctamente.");
    }
}
