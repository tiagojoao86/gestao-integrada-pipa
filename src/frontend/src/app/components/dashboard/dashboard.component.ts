import { Component, OnInit, inject } from '@angular/core';
import { BaseComponent } from '../base/base.component';

import { SystemModuleGroupComponent } from '../base/menu/system-module-group/system-module-group.component';
import { SystemModuleGroup } from '../base/menu/system-module-group/system-module-group';
import { SystemModule } from '../base/menu/system-module/system-module';
import { AuthService } from '../base/auth/auth-service';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-dashboard',
  imports: [BaseComponent, SystemModuleGroupComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  standalone: true,
})
export class DashboardComponent implements OnInit {
  title: string = $localize`Dashboards`;
  systemModuleGroups: SystemModuleGroup[] = [];

  private authService = inject(AuthService);

  ngOnInit(): void {
    this.buildGroups();
  }

  private buildGroups(): void {
    const financeiroModules: SystemModule[] = [];

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.DASHBOARD_FINANCEIRO_FLUXO_CAIXA
      )
    ) {
      financeiroModules.push({
        name: $localize`Fluxo de Caixa`,
        icon: 'waterfall_chart',
        url: '/dashboard/fluxo-caixa',
      });
    }

    if (financeiroModules.length > 0) {
      this.systemModuleGroups.push({
        name: $localize`Financeiro`,
        systemModules: financeiroModules,
      });
    }

    const atendimentoModules: SystemModule[] = [];

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.DASHBOARD_ATENDIMENTO_POR_MES
      )
    ) {
      atendimentoModules.push({
        name: $localize`Atendimentos por Mês`,
        icon: 'event_note',
        url: '/dashboard/atendimento-por-mes',
      });
    }

    if (atendimentoModules.length > 0) {
      this.systemModuleGroups.push({
        name: $localize`Atendimento`,
        systemModules: atendimentoModules,
      });
    }
  }
}
