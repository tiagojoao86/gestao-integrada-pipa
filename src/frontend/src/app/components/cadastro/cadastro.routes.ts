import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { moduleAuthorityGuard } from '../base/auth/module-authority.guard';
import { groupAuthorityGuard } from '../base/auth/group-authority.guard';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./cadastro.component').then((app) => app.CadastroComponent),
    canActivate: [authGuard, groupAuthorityGuard],
    data: {
      group: SystemModuleKey.CADASTRO,
    },
  },
  {
    path: 'usuario',
    loadComponent: () =>
      import('./usuario/usuario.component').then(
        (app) => app.UsuariosComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.CADASTRO_USUARIO,
    },
  },
  {
    path: 'perfil',
    loadComponent: () =>
      import('./perfil/perfil.component').then((app) => app.PerfilComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.CADASTRO_PERFIL,
    },
  },
  {
    path: 'pessoa',
    loadComponent: () =>
      import('./pessoa/pessoa.component').then((app) => app.PessoaComponent),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.CADASTRO_PESSOA,
    },
  },
  {
    path: 'unidade-negocio',
    loadComponent: () =>
      import('./unidade-negocio/unidade-negocio.component').then(
        (app) => app.UnidadeNegocioComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: SystemModuleKey.CADASTRO_UNIDADE_NEGOCIO,
    },
  },
];
