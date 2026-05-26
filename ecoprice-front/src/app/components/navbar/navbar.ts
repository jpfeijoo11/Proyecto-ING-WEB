import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent implements OnInit {
  nombreUsuario: string = '';
  rolUsuario: string = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    const usuarioJSON = localStorage.getItem('usuarioActual');
    if (usuarioJSON) {
      const usuario = JSON.parse(usuarioJSON);
      this.nombreUsuario = usuario.nombreCompleto || 'Juan'; // Nombre por defecto si no viene
      this.rolUsuario = usuario.rol || 'USUARIO';
    }
  }

  cerrarSesion(): void {
    // 1. Destruir credenciales
    localStorage.removeItem('usuarioActual');
    // 2. Redirigir al inicio de sesión
    this.router.navigate(['/login']);
  }
}
