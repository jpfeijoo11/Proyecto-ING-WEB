import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Interfaces de dominio ────────────────────────────────────────────────────

export interface OperacionAduanera {
  id?: number;
  numeroTracking: string;
  tipoOperacion: string;
  estado: string;
  puertoOrigen: string;
  puertoDestino: string;
  fechaRegistro?: string;
  idImportador?: number;
  codigoArancelario?: string;
  canalAforo?: string;      // VERDE | AMARILLO | ROJO
  puntajeRiesgo?: number;
}

/** Resultado del cruce de tablas por cada vector de riesgo */
export interface DetalleRiesgo {
  // Vector 1 — catalogo_riesgo_pais
  origenEnCatalogo: boolean;
  nivelRiesgoPais: string;
  puntosOrigen: number;
  motivoPais: string;
  // Vector 2 — importador_historial
  nombreImportador: string;
  infraccionesImportador: number;
  puntosImportador: number;
  importadorInfractor: boolean;
  // Vector 3 — restricciones_arancelarias
  descripcionMercancia: string;
  categoriaMercancia: string;
  requierePermiso: boolean;
  puntosMercancia: number;
  // Vector 4 — lista_negra_global
  importadorEnListaNegra: boolean;
  organismoSancionador: string;
  motivoSancion: string;
  puntosListaNegra: number;
  // Resultado final
  puntajeTotal: number;
  canalAforo: string;
  descripcionCanal: string;
}

/** Respuesta del POST /api/operaciones o GET /{id}/analisis */
export interface OperacionConRiesgoResponse {
  operacion: OperacionAduanera;
  detalleRiesgo: DetalleRiesgo;
}

/** Estadísticas globales calculadas con SQL en tiempo real */
export interface Estadisticas {
  productoCodigo: string;
  productoDescripcion: string;
  productoCategoria: string;
  productoTotalOperaciones: number;
  puertoNombre: string;
  puertoNivelRiesgo: string;
  puertoTotalOperaciones: number;
  puertoOperacionesRiesgo: number;
  empresaNombre: string;
  empresaInfracciones: number;
  empresaPaisOrigen: string;
  totalOperaciones: number;
  operacionesVerdes: number;
  operacionesAmarillos: number;
  operacionesRojas: number;
}

export interface ImportadorHistorial {
  id: number;
  nombreEmpresa: string;
  rucEmpresa: string;
  infraccionesPrevias: number;
  paisOrigen: string;
}

export interface CatalogoRiesgoPais {
  id: number;
  nombrePuertoOPais: string;
  nivelRiesgo: string;
  puntos: number;
  motivo: string;
}

export interface RestriccionArancelaria {
  id: number;
  codigoArancelario: string;
  descripcion: string;
  requierePermiso: boolean;
  categoria: string;
}

// ── Servicio ─────────────────────────────────────────────────────────────────

@Injectable({
  providedIn: 'root'
})
export class AduanaService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // ── Operaciones ───────────────────────────────────────────────────────────

  getOperaciones(): Observable<OperacionAduanera[]> {
    return this.http.get<OperacionAduanera[]>(`${this.baseUrl}/operaciones`);
  }

  /** Crea operación y devuelve el análisis de riesgo completo (JOIN en BD) */
  crearOperacion(operacion: OperacionAduanera): Observable<OperacionConRiesgoResponse> {
    return this.http.post<OperacionConRiesgoResponse>(`${this.baseUrl}/operaciones`, operacion);
  }

  /** Envía el nuevo estado como objeto JSON ({ estado: ... }), acorde al DTO CambioEstadoRequest del backend. */
  actualizarEstadoOperacion(id: number, nuevoEstado: string): Observable<OperacionAduanera> {
    return this.http.put<OperacionAduanera>(
      `${this.baseUrl}/operaciones/${id}/estado`, { estado: nuevoEstado });
  }

  getAlertasRojas(): Observable<OperacionAduanera[]> {
    return this.http.get<OperacionAduanera[]>(`${this.baseUrl}/operaciones/alerta-roja`);
  }

  /** Re-ejecuta el JOIN para una operación existente y devuelve el desglose */
  getAnalisisOperacion(id: number): Observable<OperacionConRiesgoResponse> {
    return this.http.get<OperacionConRiesgoResponse>(`${this.baseUrl}/operaciones/${id}/analisis`);
  }

  /** Estadísticas globales calculadas en tiempo real con SQL */
  getEstadisticas(): Observable<Estadisticas> {
    return this.http.get<Estadisticas>(`${this.baseUrl}/estadisticas`);
  }

  // ── Autenticación ─────────────────────────────────────────────────────────

  login(credenciales: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/admin/login`, credenciales);
  }

  registrarAdmin(usuario: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/admin/registro`, usuario);
  }

  getUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/listar`);
  }

  // ── Ubicaciones ───────────────────────────────────────────────────────────

  getPaises(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/ubicaciones/paises`);
  }

  getProvincias(paisId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/ubicaciones/provincias/${paisId}`);
  }

  getCiudades(provinciaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/ubicaciones/ciudades/${provinciaId}`);
  }

  // ── Catálogos del Motor de Riesgo ─────────────────────────────────────────

  getImportadores(): Observable<ImportadorHistorial[]> {
    return this.http.get<ImportadorHistorial[]>(`${this.baseUrl}/catalogos/importadores`);
  }

  getPaisesRiesgo(): Observable<CatalogoRiesgoPais[]> {
    return this.http.get<CatalogoRiesgoPais[]>(`${this.baseUrl}/catalogos/paises-riesgo`);
  }

  getArancelarios(): Observable<RestriccionArancelaria[]> {
    return this.http.get<RestriccionArancelaria[]>(`${this.baseUrl}/catalogos/arancelarios`);
  }

  // ── Borrado ───────────────────────────────────────────────────────────────

  eliminarUsuario(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/${id}`, { responseType: 'text' });
  }

  eliminarOperacion(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/operaciones/${id}`, { responseType: 'text' });
  }
}
