import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AduanaService } from '../services/aduana.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {

  credenciales = {
    username: '',
    password: ''
  };

  mensajeError = '';

  constructor(
    private aduanaService: AduanaService,
    private router: Router
  ) {}

  iniciarSesion(): void {
    this.mensajeError = '';

    this.aduanaService.login(this.credenciales).subscribe({
      next: (res) => {
        // Guardamos el objeto completo del usuario para tener su rol disponible en toda la app
        localStorage.setItem('usuarioActual', JSON.stringify(res));

        alert('¡Bienvenido ' + res.nombreCompleto + '!');

        // LÓGICA DE REDIRECCIÓN SEGÚN ROL (Punto 2 de la Rúbrica)
        if (res.rol === 'ADMIN') {
          // El Administrador es el único que puede alimentar el sistema con nuevos usuarios
          this.router.navigate(['/registro']);
        } else {
          // Agentes e Inspectores no tienen acceso a la administración de usuarios
          alert('Tu rol de ' + res.rol + ' no tiene permisos para gestionar usuarios.');
          // Por ahora los devolvemos al login o puedes mandarlos a un dashboard futuro
          this.router.navigate(['/login']);
        }
      },
      error: (err) => {
        // Manejo de error 401 (Unauthorized) desde Spring Boot
        this.mensajeError = 'Usuario o contraseña incorrectos. Verifica tus credenciales.';
      }
    });
  }
}
