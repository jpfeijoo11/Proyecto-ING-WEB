package com.proyecto.backend.services;

import com.proyecto.backend.dto.DetalleRiesgoDTO;
import com.proyecto.backend.events.CanalAforoAsignadoEvent;
import com.proyecto.backend.models.OperacionAduanera;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Motor de riesgo aduanero: ejecuta el cruce (JOIN) contra los catálogos de
 * riesgo y enriquece la operación con su canal de aforo.
 *
 * <p>Mejoras aplicadas sobre la versión inicial:</p>
 * <ul>
 *   <li><b>SRP</b>: la resolución del canal (umbrales) y su descripción ya
 *       no viven aquí — se delegan a {@link PoliticaCanalAforo} — y el log
 *       de la evaluación ya no se escribe directamente en este servicio,
 *       se delega a un observador vía {@link CanalAforoAsignadoEvent}. Este
 *       servicio se limita a una responsabilidad: ejecutar el cruce de
 *       catálogos y calcular el puntaje.</li>
 *   <li><b>DIP</b>: en vez de invocar métodos privados con reglas fijas o
 *       instanciar colaboradores concretos, depende de las abstracciones
 *       {@link PoliticaCanalAforo} y {@link ApplicationEventPublisher},
 *       inyectadas por constructor.</li>
 * </ul>
 */
@Service
public class PerfilRiesgoService {

    private final NamedParameterJdbcTemplate jdbc;
    private final PoliticaCanalAforo politicaCanalAforo;
    private final ApplicationEventPublisher eventPublisher;

    public PerfilRiesgoService(NamedParameterJdbcTemplate jdbc,
                                PoliticaCanalAforo politicaCanalAforo,
                                ApplicationEventPublisher eventPublisher) {
        this.jdbc = jdbc;
        this.politicaCanalAforo = politicaCanalAforo;
        this.eventPublisher = eventPublisher;
    }


    private static final String SQL_ANALISIS_RIESGO = """
        SELECT
            -- ── VECTOR 1: catalogo_riesgo_pais ──────────────────────────────
            CASE WHEN crp.id IS NOT NULL THEN true  ELSE false END  AS origen_en_catalogo,
            COALESCE(crp.nivel_riesgo, 'N/A')                        AS nivel_riesgo_pais,
            COALESCE(crp.puntos,       0)                            AS puntos_origen,
            COALESCE(crp.motivo,       'Puerto no catalogado')       AS motivo_pais,

            -- ── VECTOR 2: importador_historial ───────────────────────────────
            COALESCE(ih.nombre_empresa,      'Sin importador')       AS nombre_importador,
            COALESCE(ih.infracciones_previas, 0)                     AS infracciones_importador,
            CASE WHEN COALESCE(ih.infracciones_previas, 0) > 2
                 THEN 40 ELSE 0 END                                  AS puntos_importador,
            CASE WHEN COALESCE(ih.infracciones_previas, 0) > 2
                 THEN true ELSE false END                            AS importador_infractor,

            -- ── VECTOR 3: restricciones_arancelarias ─────────────────────────
            COALESCE(ra.descripcion,     'Mercancía general')        AS descripcion_mercancia,
            COALESCE(ra.categoria,       'GENERAL')                  AS categoria_mercancia,
            COALESCE(ra.requiere_permiso, false)                     AS requiere_permiso,
            CASE WHEN COALESCE(ra.requiere_permiso, false) = true
                 THEN 30 ELSE 0 END                                  AS puntos_mercancia,

            -- ── VECTOR 4: importador_historial ⟷ lista_negra_global ──────────
            -- JOIN entre DOS catálogos: cruza ruc_empresa del importador
            -- contra ruc_sancionado de la lista negra internacional (OFAC/ONU)
            CASE WHEN lng.id IS NOT NULL THEN true ELSE false END    AS importador_en_lista_negra,
            COALESCE(lng.organismo_sancionador, 'N/A')               AS organismo_sancionador,
            COALESCE(lng.motivo_sancion,        'Sin sancion')       AS motivo_sancion,
            CASE WHEN lng.id IS NOT NULL
                 THEN COALESCE(lng.puntos_extra, 50) ELSE 0 END      AS puntos_lista_negra,

            -- ── PUNTAJE TOTAL (4 vectores calculados en BD) ──────────────────
            (
                COALESCE(crp.puntos, 0) +
                CASE WHEN COALESCE(ih.infracciones_previas, 0) > 2 THEN 40 ELSE 0 END +
                CASE WHEN COALESCE(ra.requiere_permiso, false) = true THEN 30 ELSE 0 END +
                CASE WHEN lng.id IS NOT NULL THEN COALESCE(lng.puntos_extra, 50) ELSE 0 END
            ) AS puntaje_total

        FROM (VALUES (1)) AS dummy(x)

        LEFT JOIN catalogo_riesgo_pais crp
            ON LOWER(TRIM(crp.nombre_puerto_o_pais)) = LOWER(TRIM(:puertoOrigen))

        LEFT JOIN importador_historial ih
            ON ih.id = :idImportador

        LEFT JOIN restricciones_arancelarias ra
            ON LOWER(TRIM(ra.codigo_arancelario)) = LOWER(TRIM(:codigoArancelario))

        LEFT JOIN lista_negra_global lng
            ON LOWER(TRIM(lng.ruc_sancionado)) = LOWER(TRIM(COALESCE(ih.ruc_empresa, '')))
        """;

    /**
     * Ejecuta el análisis de riesgo mediante JOIN en PostgreSQL,
     * enriquece la operación con canal y puntaje, y devuelve el
     * desglose completo de cada vector para presentarlo al usuario.
     *
     * @param operacion Operación a evaluar (sin canalAforo/puntajeRiesgo aún)
     * @return DetalleRiesgoDTO con el resultado de los 3 cruces y el canal asignado
     */
    public DetalleRiesgoDTO evaluarYObtenerDetalle(OperacionAduanera operacion) {

        // Preparar parámetros — valores nulos son manejados por el LEFT JOIN
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("puertoOrigen",       orVacio(operacion.getPuertoOrigen()))
                .addValue("idImportador",        operacion.getIdImportador()) 
                .addValue("codigoArancelario",   orVacio(operacion.getCodigoArancelario()));

        // Ejecutar la consulta con 4 LEFT JOINs — resultado en una sola fila
        DetalleRiesgoDTO detalle = jdbc.queryForObject(SQL_ANALISIS_RIESGO, params, (rs, rowNum) -> {
            DetalleRiesgoDTO dto = new DetalleRiesgoDTO();

            // Vector 1 — origen geográfico
            dto.setOrigenEnCatalogo(rs.getBoolean("origen_en_catalogo"));
            dto.setNivelRiesgoPais(rs.getString("nivel_riesgo_pais"));
            dto.setPuntosOrigen(rs.getInt("puntos_origen"));
            dto.setMotivoPais(rs.getString("motivo_pais"));

            // Vector 2 — historial del importador
            dto.setNombreImportador(rs.getString("nombre_importador"));
            dto.setInfraccionesImportador(rs.getInt("infracciones_importador"));
            dto.setPuntosImportador(rs.getInt("puntos_importador"));
            dto.setImportadorInfractor(rs.getBoolean("importador_infractor"));

            // Vector 3 — mercancía arancelaria
            dto.setDescripcionMercancia(rs.getString("descripcion_mercancia"));
            dto.setCategoriaMercancia(rs.getString("categoria_mercancia"));
            dto.setRequierePermiso(rs.getBoolean("requiere_permiso"));
            dto.setPuntosMercancia(rs.getInt("puntos_mercancia"));

            // Vector 4 — lista negra global (JOIN entre dos catálogos)
            dto.setImportadorEnListaNegra(rs.getBoolean("importador_en_lista_negra"));
            dto.setOrganismoSancionador(rs.getString("organismo_sancionador"));
            dto.setMotivoSancion(rs.getString("motivo_sancion"));
            dto.setPuntosListaNegra(rs.getInt("puntos_lista_negra"));

            // Puntaje total calculado en BD (4 vectores)
            dto.setPuntajeTotal(rs.getInt("puntaje_total"));

            return dto;
        });

        // Asignar canal y descripción — delegado a la estrategia inyectada (Strategy / OCP)
        String canal = politicaCanalAforo.resolverCanal(detalle.getPuntajeTotal());
        detalle.setCanalAforo(canal);
        detalle.setDescripcionCanal(politicaCanalAforo.descripcionCanal(canal));

        // Enriquecer la entidad que se guardará en BD
        operacion.setPuntajeRiesgo(detalle.getPuntajeTotal());
        operacion.setCanalAforo(canal);

        // Canal VERDE → pasar directamente a desaduanización automática
        if ("VERDE".equals(canal)) {
            operacion.setEstado("DESADUANIZACION");
        }

        // Publicar evento (Observer): quien quiera reaccionar (logging, alertas
        // futuras al Inspector, WebSocket, correo) se suscribe a este evento sin
        // que este servicio necesite conocerlo ni cambiar.
        eventPublisher.publishEvent(new CanalAforoAsignadoEvent(
                operacion.getNumeroTracking(), canal, detalle.getPuntajeTotal()));

        return detalle;
    }

    /**
     * Re-ejecuta el JOIN para una operación EXISTENTE ya guardada en BD.
     * No muta la entidad. Devuelve el desglose completo para mostrarlo
     * cuando el usuario hace click en cualquier registro de la tabla.
     *
     * @param operacion Operación ya persistida con sus datos originales
     * @return DetalleRiesgoDTO con el resultado del cruce de tablas
     */
    public DetalleRiesgoDTO analizarOperacionExistente(OperacionAduanera operacion) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("puertoOrigen",     orVacio(operacion.getPuertoOrigen()))
                .addValue("idImportador",      operacion.getIdImportador())
                .addValue("codigoArancelario", orVacio(operacion.getCodigoArancelario()));

        DetalleRiesgoDTO detalle = jdbc.queryForObject(SQL_ANALISIS_RIESGO, params, (rs, rowNum) -> {
            DetalleRiesgoDTO dto = new DetalleRiesgoDTO();
            dto.setOrigenEnCatalogo(rs.getBoolean("origen_en_catalogo"));
            dto.setNivelRiesgoPais(rs.getString("nivel_riesgo_pais"));
            dto.setPuntosOrigen(rs.getInt("puntos_origen"));
            dto.setMotivoPais(rs.getString("motivo_pais"));
            dto.setNombreImportador(rs.getString("nombre_importador"));
            dto.setInfraccionesImportador(rs.getInt("infracciones_importador"));
            dto.setPuntosImportador(rs.getInt("puntos_importador"));
            dto.setImportadorInfractor(rs.getBoolean("importador_infractor"));
            dto.setDescripcionMercancia(rs.getString("descripcion_mercancia"));
            dto.setCategoriaMercancia(rs.getString("categoria_mercancia"));
            dto.setRequierePermiso(rs.getBoolean("requiere_permiso"));
            dto.setPuntosMercancia(rs.getInt("puntos_mercancia"));
            dto.setImportadorEnListaNegra(rs.getBoolean("importador_en_lista_negra"));
            dto.setOrganismoSancionador(rs.getString("organismo_sancionador"));
            dto.setMotivoSancion(rs.getString("motivo_sancion"));
            dto.setPuntosListaNegra(rs.getInt("puntos_lista_negra"));
            dto.setPuntajeTotal(rs.getInt("puntaje_total"));
            return dto;
        });

        // Usar el canal ya almacenado en la operación (el que se calculó al registrar)
        String canal = operacion.getCanalAforo() != null
                ? operacion.getCanalAforo()
                : politicaCanalAforo.resolverCanal(detalle.getPuntajeTotal());

        detalle.setCanalAforo(canal);
        detalle.setDescripcionCanal(politicaCanalAforo.descripcionCanal(canal));
        return detalle;
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private String orVacio(String valor) {
        return (valor == null || valor.isBlank()) ? "" : valor.trim();
    }
}
