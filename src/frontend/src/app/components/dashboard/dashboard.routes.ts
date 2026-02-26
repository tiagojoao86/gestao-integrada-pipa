import { Routes } from '@angular/router';
import { authGuard } from '../base/auth/auth-guard';
import { groupAuthorityGuard } from '../base/auth/group-authority.guard';
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
];
