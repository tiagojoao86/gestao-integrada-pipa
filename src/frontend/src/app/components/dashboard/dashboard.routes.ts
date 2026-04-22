import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { groupAuthorityGuard } from '../base/auth/group-authority.guard';
import { moduleAuthorityGuard } from '../base/auth/module-authority.guard';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./dashboard.component').then((app) => app.DashboardComponent),
    canActivate: [authGuard, groupAuthorityGuard],
    data: {
      group: SystemModuleKey.DASHBOARDS,
    },
  },
  {
    path: 'fluxo-caixa',
    loadComponent: () =>
      import('./dfc/dfc.component').then((app) => app.DfcComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.DASHBOARD_FINANCEIRO_FLUXO_CAIXA,
    },
  },
  {
    path: 'atendimento-por-mes',
    loadComponent: () =>
      import('./atendimento-por-mes/atendimento-por-mes.component').then(
        (app) => app.AtendimentoPorMesComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.DASHBOARD_ATENDIMENTO_POR_MES,
    },
  },
];
