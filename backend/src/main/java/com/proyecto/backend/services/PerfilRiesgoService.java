package com.proyecto.backend.services;

import com.proyecto.backend.dto.DetalleRiesgoDTO;
import com.proyecto.backend.models.OperacionAduanera;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Motor de Perfilamiento de Riesgo Aduanero — "El Semáforo de Aforos"
 *
 * Implementación técnica: ejecuta UNA SOLA consulta SQL con 4 LEFT JOINs
 * que cruza simultáneamente las tablas:
 *   - catalogo_riesgo_pais       (vector 1: origen geográfico)
 *   - importador_historial       (vector 2: historial empresarial)
 *   - restricciones_arancelarias (vector 3: mercancía restringida)
 *   - lista_negra_global         (vector 4: sanciones internacionales OFAC/ONU)
 *
 * El vector 4 es notable porque cruza DOS tablas de catálogo entre sí:
 * toma el ruc_empresa de importador_historial y lo compara contra
 * lista_negra_global.ruc_sancionado — un JOIN entre catálogos, no contra operaciones.
 *
 * El score y el canal se calculan DENTRO de la base de datos (CASE/COALESCE
 * en PostgreSQL), no en lógica Java de múltiples queries separados.
 *
 * Canal resultante:
 *   🟢 VERDE    (0  –  30 pts): Desaduanización Automática
 *   🟡 AMARILLO (31 –  70 pts): Aforo Documental
 *   🔴 ROJO     (71 – 150 pts): Aforo Físico Intrusivo
 */
@Service
public class PerfilRiesgoService {

    // ── Umbrales de canal ─────────────────────────────────────────────────────
    private static final int UMBRAL_VERDE    = 30;
    private static final int UMBRAL_AMARILLO = 70;

    /**
     * Consulta SQL con 3 LEFT JOINs.
     * Los parámetros :puertoOrigen, :idImportador, :codigoArancelario
     * actúan como claves de cruce contra las tres tablas de catálogo.
     *
     * Cuando un parámetro no coincide con ningún registro (NULL o no existe),
     * el LEFT JOIN devuelve NULL y COALESCE aplica el valor por defecto (0 / false).
     * El score final se calcula directamente en SQL con expresiones CASE.
     */
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

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

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
                .addValue("idImportador",        operacion.getIdImportador())  // puede ser null → no coincide
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

        // Asignar canal y descripción según umbrales
        String canal = resolverCanal(detalle.getPuntajeTotal());
        detalle.setCanalAforo(canal);
        detalle.setDescripcionCanal(descripcionCanal(canal));

        // Enriquecer la entidad que se guardará en BD
        operacion.setPuntajeRiesgo(detalle.getPuntajeTotal());
        operacion.setCanalAforo(canal);

        // Canal VERDE → pasar directamente a desaduanización automática
        if ("VERDE".equals(canal)) {
            operacion.setEstado("DESADUANIZACION");
        }

        // Log técnico para el servidor
        System.out.printf(
            "[PERFIL-RIESGO] %s | Origen: %s(%s) +%d | Importador: %s(%d inf.) +%d | " +
            "Mercancía: %s(perm=%s) +%d | TOTAL: %d pts → %s%n",
            operacion.getNumeroTracking(),
            operacion.getPuertoOrigen(), detalle.getNivelRiesgoPais(), detalle.getPuntosOrigen(),
            detalle.getNombreImportador(), detalle.getInfraccionesImportador(), detalle.getPuntosImportador(),
            detalle.getDescripcionMercancia(), detalle.isRequierePermiso(), detalle.getPuntosMercancia(),
            detalle.getPuntajeTotal(), canal
        );

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
                : resolverCanal(detalle.getPuntajeTotal());

        detalle.setCanalAforo(canal);
        detalle.setDescripcionCanal(descripcionCanal(canal));
        return detalle;
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private String resolverCanal(int score) {
        if (score <= UMBRAL_VERDE)    return "VERDE";
        if (score <= UMBRAL_AMARILLO) return "AMARILLO";
        return "ROJO";
    }

    private String descripcionCanal(String canal) {
        return switch (canal) {
            case "VERDE"    -> "Desaduanización Automática: carga aprobada sin inspección adicional.";
            case "AMARILLO" -> "Aforo Documental: el Agente debe subir Factura, Certificado de Origen y Póliza de Seguro.";
            case "ROJO"     -> "Aforo Físico Intrusivo: el Inspector debe abrir el contenedor presencialmente y llenar el reporte de hallazgos.";
            default         -> "";
        };
    }

    private String orVacio(String valor) {
        return (valor == null || valor.isBlank()) ? "" : valor.trim();
    }
}
