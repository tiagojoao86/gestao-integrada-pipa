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
  {
    path: 'convenio',
    loadComponent: () =>
      import('./convenio/convenio.component').then((app) => app.ConvenioComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.ATENDIMENTO_CONVENIO,
    },
  },
  {
    path: 'convenio-categoria',
    loadComponent: () =>
      import('./convenio-categoria/convenio-categoria.component').then(
        (app) => app.ConvenioCategoriaComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.ATENDIMENTO_CONVENIO_CATEGORIA,
    },
  },
  {
    path: 'procedimento',
    loadComponent: () =>
      import('./procedimento/procedimento.component').then(
        (app) => app.ProcedimentoComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.ATENDIMENTO_PROCEDIMENTO,
    },
  },
  {
    path: 'tabela',
    loadComponent: () =>
      import('./tabela/tabela.component').then((app) => app.TabelaComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.ATENDIMENTO_TABELA,
    },
  },
  {
    path: 'atendimento',
    loadComponent: () =>
      import('./atendimento/atendimento-list.component').then(
        (app) => app.AtendimentoListComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.ATENDIMENTO,
    },
  },
  {
    path: 'agendamento/agenda',
    loadComponent: () =>
      import('./agendamento/agenda/agenda.component').then((app) => app.AgendaComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.AGENDAMENTO_AGENDA,
    },
  },
  {
    path: 'agendamento/agendamento',
    loadComponent: () =>
      import('./agendamento/agendamento/agendamento.component').then(
        (app) => app.AgendamentoComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.AGENDAMENTO_AGENDAMENTO,
    },
  },
];
