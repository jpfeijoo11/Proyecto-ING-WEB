import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar';
import { AduanaService, OperacionAduanera, Estadisticas } from '../../services/aduana.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  isAdmin: boolean = false;
  isInspector: boolean = false;
  nombre: string = '';
  rolActual: string = '';

  // Alertas de Canal ROJO para el Inspector / Admin
  alertasRojas: OperacionAduanera[] = [];
  cargandoAlertas: boolean = false;

  // Estadísticas globales — visibles para TODOS los roles
  estadisticas: Estadisticas | null = null;
  cargandoEstadisticas: boolean = false;

  constructor(private aduanaService: AduanaService) {}

  ngOnInit(): void {
    const usuarioJSON = localStorage.getItem('usuarioActual');
    if (usuarioJSON) {
      const usuario = JSON.parse(usuarioJSON);
      this.rolActual   = usuario.rol?.toUpperCase() || '';
      this.isAdmin     = this.rolActual === 'ADMINISTRADOR' || this.rolActual === 'ADMIN';
      this.isInspector = this.rolActual === 'INSPECTOR';
      this.nombre      = usuario.nombreCompleto || 'Usuario';
    }

    // Estadísticas: todos los roles las ven
    this.cargarEstadisticas();

    // Alertas Canal Rojo: solo Inspector y Admin
    if (this.isInspector || this.isAdmin) {
      this.cargarAlertasRojas();
    }
  }

  cargarEstadisticas(): void {
    this.cargandoEstadisticas = true;
    this.aduanaService.getEstadisticas().subscribe({
      next: (data) => {
        this.estadisticas = data;
        this.cargandoEstadisticas = false;
      },
      error: (err) => {
        console.error('Error al cargar estadísticas', err);
        this.cargandoEstadisticas = false;
      }
    });
  }

  cargarAlertasRojas(): void {
    this.cargandoAlertas = true;
    this.aduanaService.getAlertasRojas().subscribe({
      next: (data) => {
        this.alertasRojas = data;
        this.cargandoAlertas = false;
      },
      error: (err) => {
        console.error('Error al cargar alertas rojas', err);
        this.cargandoAlertas = false;
      }
    });
  }

  /** Porcentaje de canal para la barra de progreso visual */
  getPct(valor: number, total: number): number {
    return total > 0 ? Math.round((valor / total) * 100) : 0;
  }
}
