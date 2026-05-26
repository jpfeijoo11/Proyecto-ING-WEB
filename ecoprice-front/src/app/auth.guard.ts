import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const usuarioJSON = localStorage.getItem('usuarioActual');

  // 1. Si no hay sesión, al login
  if (!usuarioJSON) {
    router.navigate(['/login']);
    return false;
  }

  // Parseamos el string para convertirlo de nuevo en un objeto de TypeScript
  const usuario = JSON.parse(usuarioJSON);
  const rolUsuario = usuario.rol?.toUpperCase();

  // 2. Protección estricta: Solo el Admin puede entrar a la ruta de registro/usuarios
  if (state.url.includes('/registro') && (rolUsuario !== 'ADMIN' && rolUsuario !== 'ADMINISTRADOR')) {
    // Si un Agente o Inspector intenta burlar la URL, lo mandamos a sus operaciones
    router.navigate(['/operaciones']);
    return false;
  }

  return true;
};
