package com.proyecto.backend.events;

/**
 * Evento de dominio (patrón Observer, implementado con el publicador de
 * eventos de Spring) emitido cada vez que {@code PerfilRiesgoService}
 * resuelve el canal de aforo de una operación.
 *
 * <p>Antes, el propio servicio de riesgo escribía directamente el log de
 * cada evaluación con {@code System.out.printf}, acoplando la lógica de
 * negocio a un mecanismo de salida concreto (violación de DIP). Ahora el
 * servicio solo publica este evento; cuántos "observadores" reaccionen a
 * él (logging, una futura notificación al Inspector, un WebSocket, un
 * correo, etc.) es una decisión de cada listener, no del motor de riesgo.</p>
 */
public class CanalAforoAsignadoEvent {

    private final String numeroTracking;
    private final String canal;
    private final int puntajeTotal;

    public CanalAforoAsignadoEvent(String numeroTracking, String canal, int puntajeTotal) {
        this.numeroTracking = numeroTracking;
        this.canal = canal;
        this.puntajeTotal = puntajeTotal;
    }

    public String getNumeroTracking() { return numeroTracking; }
    public String getCanal() { return canal; }
    public int getPuntajeTotal() { return puntajeTotal; }
}
