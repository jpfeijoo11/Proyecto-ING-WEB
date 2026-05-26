import { Routes } from '@angular/router';
import { LoginComponent } from './login/login';
import { RegistroAdminComponent } from './components/registro-admin/registro-admin.component';
import { OperacionesComponent } from './components/operaciones/operaciones';
import { DashboardComponent } from './components/dashboard/dashboard';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  // Ruta inicial: Redirige automáticamente al Login
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // Rutas Públicas
  { path: 'login', component: LoginComponent },

  // Rutas Privadas / Protegidas por Seguridad (Requieren Token y Sesión)
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'registro',
    component: RegistroAdminComponent,
    canActivate: [authGuard]
  },
  {
    path: 'operaciones',
    component: OperacionesComponent,
    canActivate: [authGuard]
  },

  // Ruta Comodín: Protege contra URLs rotas o inexistentes
  { path: '**', redirectTo: 'login' }
];
