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

  // ID del usuario logueado (para no permitir que se borre a sí mismo)
  idUsuarioActual: number | null = null;

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

    // Guardar ID del usuario logueado para proteger su propia cuenta
    if (usuarioJson) {
      const user = JSON.parse(usuarioJson);
      this.idUsuarioActual = user.id ?? null;
    }

    // Cargar países al iniciar
    this.aduanaService.getPaises().subscribe(data => {
      this.paises = data;
    });

    // Cargar la lista de usuarios inmediatamente
    this.cargarUsuarios();
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

  // Carga (o recarga) la lista de usuarios — no hace toggle, solo actualiza
  cargarUsuarios(): void {
    this.aduanaService.getUsuarios().subscribe({
      next: (data) => {
        this.usuariosRegistrados = data;
        this.mostrarTabla = true;
      },
      error: () => alert('No se pudo conectar con el servidor para listar usuarios.')
    });
  }

  // Método para alternar visibilidad de tabla (botón opcional)
  consultarUsuarios(): void {
    if (this.mostrarTabla) {
      this.mostrarTabla = false;
    } else {
      this.cargarUsuarios();
    }
  }

  // Eliminar un usuario por ID
  eliminarUsuario(usuario: any): void {
    if (usuario.id === this.idUsuarioActual) {
      alert('⚠️ No puedes eliminar tu propia cuenta de administrador.');
      return;
    }
    const confirmar = confirm(
      `¿Eliminar al usuario "${usuario.nombreCompleto}" (${usuario.rol})?\n\nEsta acción no se puede deshacer.`
    );
    if (!confirmar) return;

    this.aduanaService.eliminarUsuario(usuario.id).subscribe({
      next: () => {
        this.mensajeExito = `Usuario "${usuario.nombreCompleto}" eliminado correctamente.`;
        this.mensajeError = '';
        // Actualizar la lista sin volver a pedir al servidor (actualización inmediata)
        this.usuariosRegistrados = this.usuariosRegistrados.filter(u => u.id !== usuario.id);
      },
      error: () => {
        this.mensajeError = 'No se pudo eliminar el usuario. Intenta de nuevo.';
        this.mensajeExito = '';
      }
    });
  }
}
