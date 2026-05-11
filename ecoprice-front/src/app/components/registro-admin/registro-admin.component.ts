import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AduanaService } from '../../services/aduana.service';

@Component({
  selector: 'app-registro-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './registro-admin.component.html'
})
export class RegistroAdminComponent implements OnInit {

  // Objeto para el formulario de registro
  usuario = {
    username: '',
    password: '',
    cedula: '',
    nombreCompleto: '',
    rol: ''
  };

  // Variables para ubicación
  paises: any[] = [];
  provincias: any[] = [];
  ciudades: any[] = [];
  paisSeleccionado: any = null;
  provinciaSeleccionada: any = null;
  ciudadSeleccionada: any = null;

  // Variables para la consulta de usuarios
  usuariosRegistrados: any[] = [];
  mostrarTabla: boolean = false;

  // Mensajes de feedback
  mensajeError: string = '';
  mensajeExito: string = '';

  constructor(
    private aduanaService: AduanaService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // PROTECCIÓN DE RUTA: Solo el ADMIN puede estar aquí
    const usuarioJson = localStorage.getItem('usuarioActual');
    if (usuarioJson) {
      const user = JSON.parse(usuarioJson);
      if (user.rol !== 'ADMIN') {
        alert('Acceso denegado. Solo administradores pueden gestionar usuarios.');
        this.router.navigate(['/login']);
      }
    } else {
      this.router.navigate(['/login']);
    }

    // Cargar países al iniciar
    this.aduanaService.getPaises().subscribe(data => {
      this.paises = data;
    });
  }

  // Lógica de ubicación
  onPaisChange(): void {
    this.provincias = [];
    this.ciudades = [];
    if (this.paisSeleccionado) {
      this.aduanaService.getProvincias(this.paisSeleccionado.id).subscribe(data => {
        this.provincias = data;
      });
    }
  }

  onProvinciaChange(): void {
    this.ciudades = [];
    if (this.provinciaSeleccionada) {
      this.aduanaService.getCiudades(this.provinciaSeleccionada.id).subscribe(data => {
        this.ciudades = data;
      });
    }
  }

  // Método para registrar nuevos usuarios (Agentes/Inspectores)
  registrar(): void {
    this.mensajeError = '';
    this.mensajeExito = '';

    this.aduanaService.registrarAdmin(this.usuario).subscribe({
      next: (res) => {
        this.mensajeExito = '¡Usuario creado con éxito en la base de datos!';
        // Limpiar formulario
        this.usuario = { username: '', password: '', cedula: '', nombreCompleto: '', rol: '' };
        // Si la tabla está abierta, la actualizamos automáticamente
        if (this.mostrarTabla) this.consultarUsuarios();
      },
      error: (err) => {
        if (err.status === 400 && err.error.cedula) {
          this.mensajeError = err.error.cedula;
        } else {
          this.mensajeError = 'Ocurrió un error al intentar registrar. Revisa los datos o si el usuario ya existe.';
        }
      }
    });
  }

  // Método para consultar la base de datos
  consultarUsuarios(): void {
    this.aduanaService.getUsuarios().subscribe({
      next: (data) => {
        this.usuariosRegistrados = data;
        this.mostrarTabla = !this.mostrarTabla;
      },
      error: () => alert('No se pudo conectar con el servidor para listar usuarios.')
    });
  }
}
