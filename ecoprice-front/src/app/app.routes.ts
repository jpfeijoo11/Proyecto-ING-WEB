import { Routes } from '@angular/router';
import { LoginComponent } from './login/login'; // <-- Ruta ajustada a tu carpeta
// Asegúrate de que esta ruta al registro sea correcta según dónde lo tengas guardado
import { RegistroAdminComponent } from './components/registro-admin/registro-admin.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'registro', component: RegistroAdminComponent },
  //{ path: 'dashboard', component: DashboardComponent }
];
