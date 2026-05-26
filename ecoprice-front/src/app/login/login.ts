import { Component, OnInit } from '@angular/core';
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
export class LoginComponent implements OnInit {

  credenciales = {
    username: '',
    password: ''
  };

  mensajeError = '';

  constructor(
    private aduanaService: AduanaService,
    private router: Router
  ) {}

  // Ciclo de vida: Limpia credenciales si el usuario regresa al login
  ngOnInit(): void {
    localStorage.removeItem('usuarioActual');
  }

  iniciarSesion(): void {
    this.mensajeError = '';

    this.aduanaService.login(this.credenciales).subscribe({
      next: (res) => {
        // Almacenamos el objeto de sesión serializado
        localStorage.setItem('usuarioActual', JSON.stringify(res));

        alert('¡Bienvenido ' + res.nombreCompleto + '!');

        // Flujo unificado: Todos los roles ingresan a la pantalla de bienvenida (Hub Central)
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        // Captura de error de credenciales desde Spring Boot
        this.mensajeError = 'Usuario o contraseña incorrectos. Verifica tus credenciales.';
      }
    });
  }
}
