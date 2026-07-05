package com.proyecto.backend.services;

/**
 * Estrategia (patrón Strategy) para resolver el canal de aforo a partir del
 * puntaje de riesgo calculado por {@link PerfilRiesgoService}.
 *
 * <p>Antes, los umbrales y las descripciones de cada canal estaban
 * codificados directamente dentro de {@code PerfilRiesgoService}
 * (if/switch), violando el principio Abierto/Cerrado (OCP): para agregar
 * un nuevo canal o cambiar los umbrales había que modificar el servicio.
 * Con esta interfaz, cualquier nueva política de clasificación (por
 * ejemplo, umbrales distintos por tipo de operación, o un cuarto canal)
 * se agrega implementando esta interfaz, sin tocar el servicio que la usa.</p>
 */
public interface PoliticaCanalAforo {

    /** Resuelve el canal (VERDE / AMARILLO / ROJO) a partir del puntaje total. */
    String resolverCanal(int puntajeTotal);

    /** Describe la acción operativa que implica el canal asignado. */
    String descripcionCanal(String canal);
}
