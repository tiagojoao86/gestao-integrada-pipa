import { Component, OnInit, inject } from '@angular/core';
import { BaseComponent } from '../base/base.component';

import { SystemModuleGroupComponent } from '../base/menu/system-module-group/system-module-group.component';
import { AuthService } from '../base/auth/auth-service';
import { SystemModuleGroup } from '../base/menu/system-module-group/system-module-group';
import { SystemModule } from '../base/menu/system-module/system-module';

@Component({
  selector: 'gi-cadastro',
  imports: [BaseComponent, SystemModuleGroupComponent],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.css',
  standalone: true,
})
export class CadastroComponent implements OnInit {
  title: string = $localize`Cadastros`;
  systemModules: SystemModuleGroup[] = [];

  private authService: AuthService = inject(AuthService);

  ngOnInit(): void {
    const systemModulesCadastros: SystemModule[] = [];
    if (this.authService.hasAuthorityListarToModulo('CADASTRO_PESSOA')) {
      systemModulesCadastros.push({
        name: $localize`Pessoas`,
        icon: 'groups',
        url: '/cadastro/pessoa',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo('CADASTRO_UNIDADE_NEGOCIO')
    ) {
      systemModulesCadastros.push({
        name: $localize`Unidades de Negócio`,
        icon: 'business',
        url: '/cadastro/unidade-negocio',
      });
    }

    if (systemModulesCadastros.length > 0) {
      this.systemModules.push({
        name: $localize`Geral`,
        systemModules: systemModulesCadastros,
      });
    }
    const systemModulesGeral: SystemModule[] = [];
    if (this.authService.hasAuthorityListarToModulo('CADASTRO_USUARIO')) {
      systemModulesGeral.push({
        name: $localize`Usuários`,
        icon: 'person',
        url: '/cadastro/usuario',
      });
    }

    if (this.authService.hasAuthorityListarToModulo('CADASTRO_PERFIL')) {
      systemModulesGeral.push({
        name: $localize`Perfis`,
        icon: 'badge',
        url: '/cadastro/perfil',
      });
    }

    if (systemModulesGeral.length > 0) {
      this.systemModules.push({
        name: $localize`Sistema`,
        systemModules: systemModulesGeral,
      });
    }
  }
}
