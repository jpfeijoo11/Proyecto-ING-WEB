import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AduanaService {

  // Asegúrate de que esta sea la URL base de tu Spring Boot
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // 1. LOGIN
  login(credenciales: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/admin/login`, credenciales);
  }

  // 2. REGISTRO
  registrarAdmin(usuario: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/admin/registro`, usuario);
  }

  // 3. CONSULTAR USUARIOS (El que acabamos de agregar)
  getUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/listar`);
  }

  // --- MÉTODOS PARA LAS UBICACIONES ---
  // (Ajusta las rutas '/ubicaciones/...' si en tu Spring Boot les pusiste otro nombre)

  getPaises(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/ubicaciones/paises`);
  }

  getProvincias(paisId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/ubicaciones/provincias/${paisId}`);
  }

  getCiudades(provinciaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/ubicaciones/ciudades/${provinciaId}`);
  }
}
