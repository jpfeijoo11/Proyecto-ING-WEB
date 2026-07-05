package com.proyecto.backend.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador (patrón Observer) del evento {@link CanalAforoAsignadoEvent}.
 *
 * Se encarga exclusivamente de dejar constancia técnica de la evaluación de
 * riesgo y de marcar con una alerta las operaciones que caen en canal ROJO
 * (las que requieren aforo físico e intervención del Inspector). Si mañana
 * se agrega un canal WebSocket o un correo al Inspector, se crea otro
 * listener nuevo para {@code CanalAforoAsignadoEvent}: PerfilRiesgoService
 * no se entera ni necesita cambiar.
 */
@Component
public class NotificacionCanalRojoListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionCanalRojoListener.class);

    @EventListener
    public void onCanalAsignado(CanalAforoAsignadoEvent evento) {
        if ("ROJO".equals(evento.getCanal())) {
            log.warn("[ALERTA INSPECTOR] Operación {} asignada a canal ROJO ({} pts) — requiere aforo físico.",
                    evento.getNumeroTracking(), evento.getPuntajeTotal());
        } else {
            log.info("[PERFIL-RIESGO] Operación {} evaluada → canal {} ({} pts).",
                    evento.getNumeroTracking(), evento.getCanal(), evento.getPuntajeTotal());
        }
    }
}
