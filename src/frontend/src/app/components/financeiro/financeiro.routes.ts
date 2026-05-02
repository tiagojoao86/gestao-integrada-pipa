import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { moduleAuthorityGuard } from '../base/auth/module-authority.guard';
import { groupAuthorityGuard } from '../base/auth/group-authority.guard';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./financeiro.component').then((app) => app.FinanceiroComponent),
    canActivate: [authGuard, groupAuthorityGuard],
    data: {
      group: SystemModuleKey.FINANCEIRO,
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
      moduleKey: SystemModuleKey.FINANCEIRO_CONTA_BANCARIA,
    },
  },
  {
    path: 'titulo-categoria',
    loadComponent: () =>
      import('./titulo-categoria/titulo-categoria.component').then(
        (app) => app.TituloCategoriaComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA,
    },
  },
  {
    path: 'titulo',
    loadComponent: () =>
      import('./titulo/titulo.component').then((app) => app.TituloComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.FINANCEIRO_TITULO,
    },
  },
  {
    path: 'movimentacao',
    loadComponent: () =>
      import(
        './movimentacao-financeira/movimentacao-financeira.component'
      ).then((app) => app.MovimentacaoFinanceiraComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.FINANCEIRO_MOVIMENTACAO_FINANCEIRA,
    },
  },
  {
    path: 'centro-custo',
    loadComponent: () =>
      import('./centro-custo/centro-custo.component').then(
        (app) => app.CentroCustoComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.FINANCEIRO_CENTRO_CUSTO,
    },
  },
  {
    path: 'condicao-pagamento',
    loadComponent: () =>
      import('./condicao-pagamento/condicao-pagamento.component').then(
        (app) => app.CondicaoPagamentoComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.FINANCEIRO_CONDICAO_PAGAMENTO,
    },
  },
  {
    path: 'caixa',
    loadComponent: () =>
      import('./caixa/caixa.component').then((app) => app.CaixaComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.CADASTRO_CAIXA,
    },
  },
];
