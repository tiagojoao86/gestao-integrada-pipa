import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./home/home.component').then((app) => app.HomeComponent),
    canActivate: [authGuard],
  },
  {
    path: 'cadastro',
    loadChildren: () =>
      import('../cadastro/cadastro.routes').then(
        (cadastroRoutes) => cadastroRoutes.routes
      ),
    canActivate: [authGuard],
  },
  {
    path: 'financeiro',
    loadChildren: () =>
      import('../financeiro/financeiro.routes').then(
        (financeiroRoutes) => financeiroRoutes.routes
      ),
    canActivate: [authGuard],
  },
];
