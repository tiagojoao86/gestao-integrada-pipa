import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./components/login/login.component').then(
        (login) => login.LoginComponent
      ),
  },
  {
    path: '',
    loadComponent: () =>
      import('./components/principal/principal.component').then(
        (app) => app.PrincipalComponent
      ),
    loadChildren: () =>
      import('./components/principal/principal.routes').then(
        (principalRoutes) => principalRoutes.routes
      ),
  },
];
