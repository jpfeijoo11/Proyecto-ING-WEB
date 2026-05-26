package com.proyecto.backend.dto;

/**
 * Estadísticas globales del sistema calculadas con queries SQL JOIN/GROUP BY.
 * Visible para todos los roles en el dashboard.
 */
public class EstadisticasDTO {

    // ── Producto más solicitado ───────────────────────────────────────────────
    private String productoCodigo;
    private String productoDescripcion;
    private String productoCategoria;
    private long   productoTotalOperaciones;

    // ── Puerto con más operaciones de riesgo ──────────────────────────────────
    private String puertoNombre;
    private String puertoNivelRiesgo;
    private long   puertoTotalOperaciones;
    private long   puertoOperacionesRiesgo;   // Canal ROJO o AMARILLO

    // ── Empresa con más infracciones ──────────────────────────────────────────
    private String empresaNombre;
    private int    empresaInfracciones;
    private String empresaPaisOrigen;

    // ── Totales generales ─────────────────────────────────────────────────────
    private long totalOperaciones;
    private long operacionesVerdes;
    private long operacionesAmarillos;
    private long operacionesRojas;

    // Getters & Setters
    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }

    public String getProductoDescripcion() { return productoDescripcion; }
    public void setProductoDescripcion(String productoDescripcion) { this.productoDescripcion = productoDescripcion; }

    public String getProductoCategoria() { return productoCategoria; }
    public void setProductoCategoria(String productoCategoria) { this.productoCategoria = productoCategoria; }

    public long getProductoTotalOperaciones() { return productoTotalOperaciones; }
    public void setProductoTotalOperaciones(long productoTotalOperaciones) { this.productoTotalOperaciones = productoTotalOperaciones; }

    public String getPuertoNombre() { return puertoNombre; }
    public void setPuertoNombre(String puertoNombre) { this.puertoNombre = puertoNombre; }

    public String getPuertoNivelRiesgo() { return puertoNivelRiesgo; }
    public void setPuertoNivelRiesgo(String puertoNivelRiesgo) { this.puertoNivelRiesgo = puertoNivelRiesgo; }

    public long getPuertoTotalOperaciones() { return puertoTotalOperaciones; }
    public void setPuertoTotalOperaciones(long puertoTotalOperaciones) { this.puertoTotalOperaciones = puertoTotalOperaciones; }

    public long getPuertoOperacionesRiesgo() { return puertoOperacionesRiesgo; }
    public void setPuertoOperacionesRiesgo(long puertoOperacionesRiesgo) { this.puertoOperacionesRiesgo = puertoOperacionesRiesgo; }

    public String getEmpresaNombre() { return empresaNombre; }
    public void setEmpresaNombre(String empresaNombre) { this.empresaNombre = empresaNombre; }

    public int getEmpresaInfracciones() { return empresaInfracciones; }
    public void setEmpresaInfracciones(int empresaInfracciones) { this.empresaInfracciones = empresaInfracciones; }

    public String getEmpresaPaisOrigen() { return empresaPaisOrigen; }
    public void setEmpresaPaisOrigen(String empresaPaisOrigen) { this.empresaPaisOrigen = empresaPaisOrigen; }

    public long getTotalOperaciones() { return totalOperaciones; }
    public void setTotalOperaciones(long totalOperaciones) { this.totalOperaciones = totalOperaciones; }

    public long getOperacionesVerdes() { return operacionesVerdes; }
    public void setOperacionesVerdes(long operacionesVerdes) { this.operacionesVerdes = operacionesVerdes; }

    public long getOperacionesAmarillos() { return operacionesAmarillos; }
    public void setOperacionesAmarillos(long operacionesAmarillos) { this.operacionesAmarillos = operacionesAmarillos; }

    public long getOperacionesRojas() { return operacionesRojas; }
    public void setOperacionesRojas(long operacionesRojas) { this.operacionesRojas = operacionesRojas; }
}
