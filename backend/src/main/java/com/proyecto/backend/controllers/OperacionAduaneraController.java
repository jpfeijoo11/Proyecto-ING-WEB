package com.proyecto.backend.controllers;

import com.proyecto.backend.dto.DetalleRiesgoDTO;
import com.proyecto.backend.dto.OperacionConRiesgoResponse;
import com.proyecto.backend.dto.EstadisticasDTO;
import com.proyecto.backend.models.OperacionAduanera;
import com.proyecto.backend.repositories.OperacionAduaneraRepository;
import com.proyecto.backend.services.PerfilRiesgoService;
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

    // ── GET todas las operaciones ─────────────────────────────────────────────
    @GetMapping
    public List<OperacionAduanera> obtenerTodas() {
        return operacionRepository.findAll();
    }

    /**
     * POST /api/operaciones
     *
     * Flujo completo:
     *  1. Forzar estado inicial "DOCUMENTACION"
     *  2. Ejecutar el motor de riesgo: una consulta SQL con 3 LEFT JOINs
     *     cruzando catalogo_riesgo_pais, importador_historial y restricciones_arancelarias
     *  3. Si canal = VERDE → estado cambia a "DESADUANIZACION" (flujo automático)
     *  4. Persistir la operación con canalAforo y puntajeRiesgo
     *  5. Devolver OperacionConRiesgoResponse con la operación guardada
     *     Y el desglose completo (qué encontró cada JOIN, cuántos pts sumó)
     */
    @PostMapping
    public ResponseEntity<OperacionConRiesgoResponse> crearOperacion(
            @RequestBody OperacionAduanera operacion) {

        // 1. Estado inicial del flujo
        operacion.setEstado("DOCUMENTACION");

        // 2. Motor de riesgo: SQL JOIN → enriquece 'operacion' (canalAforo, puntajeRiesgo, estado)
        //    y devuelve el desglose por vector
        DetalleRiesgoDTO detalle = perfilRiesgoService.evaluarYObtenerDetalle(operacion);

        // 3. Persistir operación ya enriquecida
        OperacionAduanera guardada = operacionRepository.save(operacion);

        // 4. Respuesta completa: operación + análisis de riesgo
        return ResponseEntity.ok(new OperacionConRiesgoResponse(guardada, detalle));
    }

    // ── GET por tracking ──────────────────────────────────────────────────────
    @GetMapping("/tracking/{numeroTracking}")
    public ResponseEntity<OperacionAduanera> obtenerPorTracking(
            @PathVariable String numeroTracking) {
        return operacionRepository.findByNumeroTracking(numeroTracking)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── PUT avanzar estado ────────────────────────────────────────────────────
    @PutMapping("/{id}/estado")
    public ResponseEntity<OperacionAduanera> actualizarEstado(
            @PathVariable Long id,
            @RequestBody String nuevoEstado) {
        return operacionRepository.findById(id)
                .map(operacion -> {
                    String estadoLimpio = nuevoEstado.replace("\"", "");
                    operacion.setEstado(estadoLimpio);
                    return ResponseEntity.ok(operacionRepository.save(operacion));
                })
                .orElse(ResponseEntity.notFound().build());
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
