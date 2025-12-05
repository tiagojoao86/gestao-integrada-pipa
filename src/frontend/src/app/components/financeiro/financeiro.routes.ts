import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { moduleAuthorityGuard } from '../base/auth/module-authority.guard';
import { groupAuthorityGuard } from '../base/auth/group-authority.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./financeiro.component').then((app) => app.FinanceiroComponent),
    canActivate: [authGuard, groupAuthorityGuard],
    data: {
      group: 'FINANCEIRO',
    },
  },
  /*{
    path: 'plano-contas',
    loadComponent: () =>
      import('./plano-contas/plano-contas.component').then(
        (app) => app.PlanoContasComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: 'CADASTRO_PLANO_CONTAS',
    },
  },
  {
    path: 'conta-bancaria',
    loadComponent: () =>
      import('./conta-bancaria/conta-bancaria.component').then(
        (app) => app.ContaBancariaComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: 'CADASTRO_CONTA_BANCARIA',
    },
  },
  {
    path: 'titulo',
    loadComponent: () =>
      import('./titulo/titulo.component').then((app) => app.TituloComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: 'FINANCEIRO_TITULO',
    },
  },
  {
    path: 'movimentacao',
    loadComponent: () =>
      import('./movimentacao/movimentacao.component').then(
        (app) => app.MovimentacaoComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: 'FINANCEIRO_MOVIMENTACAO',
    },
  },*/
];
