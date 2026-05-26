package com.proyecto.backend.services;

import com.proyecto.backend.dto.EstadisticasDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Calcula estadísticas globales del sistema mediante consultas SQL
 * con JOIN, GROUP BY y funciones de agregación directas en PostgreSQL.
 *
 * Consultas ejecutadas:
 *  1. Producto más importado    → GROUP BY codigo_arancelario + LEFT JOIN restricciones
 *  2. Puerto con más riesgo     → GROUP BY puerto_origen + LEFT JOIN catalogo_riesgo_pais
 *  3. Empresa con más infracc.  → MAX(infracciones_previas) en importador_historial
 *  4. Resumen por canal de aforo → COUNT + GROUP BY canal_aforo
 */
@Service
public class EstadisticasService {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    public EstadisticasDTO obtenerEstadisticas() {
        EstadisticasDTO dto = new EstadisticasDTO();

        calcularTotalesPorCanal(dto);
        calcularProductoMasSolicitado(dto);
        calcularPuertoMasRiesgoso(dto);
        calcularEmpresaMasInfractora(dto);

        return dto;
    }

    // ── 1. Totales por canal ─────────────────────────────────────────────────
    private void calcularTotalesPorCanal(EstadisticasDTO dto) {
        String sql = """
            SELECT
                COUNT(*)                                                          AS total,
                SUM(CASE WHEN canal_aforo = 'VERDE'    THEN 1 ELSE 0 END)        AS verdes,
                SUM(CASE WHEN canal_aforo = 'AMARILLO' THEN 1 ELSE 0 END)        AS amarillos,
                SUM(CASE WHEN canal_aforo = 'ROJO'     THEN 1 ELSE 0 END)        AS rojas
            FROM operaciones_aduaneras
            """;
        Map<String, Object> row = jdbc.queryForMap(sql, EmptySqlParameterSource.INSTANCE);
        dto.setTotalOperaciones(toLong(row.get("total")));
        dto.setOperacionesVerdes(toLong(row.get("verdes")));
        dto.setOperacionesAmarillos(toLong(row.get("amarillos")));
        dto.setOperacionesRojas(toLong(row.get("rojas")));
    }

    // ── 2. Producto más solicitado ───────────────────────────────────────────
    private void calcularProductoMasSolicitado(EstadisticasDTO dto) {
        String sql = """
            SELECT
                oa.codigo_arancelario                                             AS codigo,
                COALESCE(ra.descripcion, oa.codigo_arancelario, 'Sin código')     AS descripcion,
                COALESCE(ra.categoria, 'GENERAL')                                 AS categoria,
                COUNT(oa.id)                                                      AS total
            FROM operaciones_aduaneras oa
            LEFT JOIN restricciones_arancelarias ra
                ON LOWER(ra.codigo_arancelario) = LOWER(oa.codigo_arancelario)
            WHERE oa.codigo_arancelario IS NOT NULL
              AND TRIM(oa.codigo_arancelario) <> ''
            GROUP BY oa.codigo_arancelario, ra.descripcion, ra.categoria
            ORDER BY total DESC
            LIMIT 1
            """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, EmptySqlParameterSource.INSTANCE);
        if (!rows.isEmpty()) {
            Map<String, Object> r = rows.get(0);
            dto.setProductoCodigo((String) r.get("codigo"));
            dto.setProductoDescripcion((String) r.get("descripcion"));
            dto.setProductoCategoria((String) r.get("categoria"));
            dto.setProductoTotalOperaciones(toLong(r.get("total")));
        } else {
            dto.setProductoCodigo("—");
            dto.setProductoDescripcion("Sin registros aún");
            dto.setProductoCategoria("—");
            dto.setProductoTotalOperaciones(0);
        }
    }

    // ── 3. Puerto con más operaciones de riesgo ──────────────────────────────
    private void calcularPuertoMasRiesgoso(EstadisticasDTO dto) {
        String sql = """
            SELECT
                oa.puerto_origen                                                  AS puerto,
                COALESCE(crp.nivel_riesgo, 'N/A')                                AS nivel_riesgo,
                COUNT(oa.id)                                                      AS total_ops,
                SUM(CASE WHEN oa.canal_aforo IN ('ROJO','AMARILLO')
                         THEN 1 ELSE 0 END)                                      AS ops_riesgo
            FROM operaciones_aduaneras oa
            LEFT JOIN catalogo_riesgo_pais crp
                ON LOWER(TRIM(crp.nombre_puerto_o_pais)) = LOWER(TRIM(oa.puerto_origen))
            WHERE oa.puerto_origen IS NOT NULL
              AND TRIM(oa.puerto_origen) <> ''
            GROUP BY oa.puerto_origen, crp.nivel_riesgo
            ORDER BY ops_riesgo DESC, total_ops DESC
            LIMIT 1
            """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, EmptySqlParameterSource.INSTANCE);
        if (!rows.isEmpty()) {
            Map<String, Object> r = rows.get(0);
            dto.setPuertoNombre((String) r.get("puerto"));
            dto.setPuertoNivelRiesgo((String) r.get("nivel_riesgo"));
            dto.setPuertoTotalOperaciones(toLong(r.get("total_ops")));
            dto.setPuertoOperacionesRiesgo(toLong(r.get("ops_riesgo")));
        } else {
            dto.setPuertoNombre("—");
            dto.setPuertoNivelRiesgo("—");
            dto.setPuertoTotalOperaciones(0);
            dto.setPuertoOperacionesRiesgo(0);
        }
    }

    // ── 4. Empresa con más infracciones ──────────────────────────────────────
    private void calcularEmpresaMasInfractora(EstadisticasDTO dto) {
        String sql = """
            SELECT nombre_empresa, infracciones_previas, pais_origen
            FROM importador_historial
            ORDER BY infracciones_previas DESC
            LIMIT 1
            """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, EmptySqlParameterSource.INSTANCE);
        if (!rows.isEmpty()) {
            Map<String, Object> r = rows.get(0);
            dto.setEmpresaNombre((String) r.get("nombre_empresa"));
            dto.setEmpresaInfracciones(((Number) r.get("infracciones_previas")).intValue());
            dto.setEmpresaPaisOrigen((String) r.get("pais_origen"));
        } else {
            dto.setEmpresaNombre("—");
            dto.setEmpresaInfracciones(0);
            dto.setEmpresaPaisOrigen("—");
        }
    }

    private long toLong(Object val) {
        return val == null ? 0L : ((Number) val).longValue();
    }
}
