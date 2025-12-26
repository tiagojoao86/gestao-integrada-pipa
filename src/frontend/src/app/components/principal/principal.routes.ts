import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

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
    path: SystemModuleKey.FINANCEIRO,
    loadChildren: () =>
      import('../financeiro/financeiro.routes').then(
        (financeiroRoutes) => financeiroRoutes.routes
      ),
    canActivate: [authGuard],
  },
];
