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
  {
    path: 'dashboard',
    loadChildren: () =>
      import('../dashboard/dashboard.routes').then(
        (dashboardRoutes) => dashboardRoutes.routes
      ),
    canActivate: [authGuard],
  },
  {
    path: 'atendimento',
    loadChildren: () =>
      import('../atendimento/atendimento.routes').then(
        (atendimentoRoutes) => atendimentoRoutes.routes
      ),
    canActivate: [authGuard],
  },
];
