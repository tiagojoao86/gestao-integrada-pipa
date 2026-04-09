import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { moduleAuthorityGuard } from '../base/auth/module-authority.guard';
import { groupAuthorityGuard } from '../base/auth/group-authority.guard';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./atendimento.component').then((app) => app.AtendimentoComponent),
    canActivate: [authGuard, groupAuthorityGuard],
    data: {
      group: 'ATENDIMENTO',
    },
  },
  {
    path: 'profissional',
    loadComponent: () =>
      import('./profissional/profissional.component').then(
        (app) => app.ProfissionalComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.ATENDIMENTO_PROFISSIONAL,
    },
  },
];
