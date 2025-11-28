import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { moduleAuthorityGuard } from '../base/auth/module-authority.guard';
import { groupAuthorityGuard } from '../base/auth/group-authority.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./cadastro.component').then((app) => app.CadastroComponent),
    canActivate: [authGuard, groupAuthorityGuard],
    data: {
      group: 'CADASTROS'
    }
  },
  {
    path: 'usuario',
    loadComponent: () =>
      import('./usuario/usuario.component').then(
        (app) => app.UsuariosComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: 'CADASTRO_USUARIO'
    }
  },
  {
    path: 'perfil',
    loadComponent: () =>
      import('./perfil/perfil.component').then(
        (app) => app.PerfilComponent
      ),
    canActivate: [authGuard, moduleAuthorityGuard],
    data: {
      moduleKey: 'CADASTRO_PERFIL'
    }
  },
];
