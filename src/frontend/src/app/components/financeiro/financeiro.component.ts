import { Component, OnInit, inject } from '@angular/core';
import { BaseComponent } from '../base/base.component';

import { SystemModuleGroupComponent } from '../base/menu/system-module-group/system-module-group.component';
import { AuthService } from '../base/auth/auth-service';
import { SystemModule } from '../base/menu/system-module/system-module';
import { SystemModuleGroup } from '../base/menu/system-module-group/system-module-group';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-financeiro',
  imports: [BaseComponent, SystemModuleGroupComponent],
  templateUrl: './financeiro.component.html',
  styleUrl: './financeiro.component.css',
  standalone: true,
})
export class FinanceiroComponent implements OnInit {
  title: string = $localize`Financeiro`;
  systemModuleGroups: SystemModuleGroup[] = [];

  private authService: AuthService = inject(AuthService);

  ngOnInit(): void {
    const cadastrosModules: SystemModule[] = [];

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.FINANCEIRO_CONTA_BANCARIA
      )
    ) {
      cadastrosModules.push({
        name: $localize`Contas Bancárias`,
        icon: 'account_balance',
        url: '/financeiro/conta-bancaria',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA
      )
    ) {
      cadastrosModules.push({
        name: $localize`Categorias de Título`,
        icon: 'category',
        url: '/financeiro/titulo-categoria',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.FINANCEIRO_CENTRO_CUSTO
      )
    ) {
      cadastrosModules.push({
        name: $localize`Centros de Custo`,
        icon: 'paid',
        url: '/financeiro/centro-custo',
      });
    }

    const financeiroModules: SystemModule[] = [];

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.FINANCEIRO_TITULO
      )
    ) {
      financeiroModules.push({
        name: $localize`Títulos`,
        icon: 'receipt_long',
        url: '/financeiro/titulo',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.FINANCEIRO_MOVIMENTACAO_FINANCEIRA
      )
    ) {
      financeiroModules.push({
        name: $localize`Movimentações`,
        icon: 'payments',
        url: '/financeiro/movimentacao',
      });
    }

    if (financeiroModules.length > 0) {
      this.systemModuleGroups.push({
        name: $localize`Operações`,
        systemModules: financeiroModules,
      });
    }

    if (cadastrosModules.length > 0) {
      this.systemModuleGroups.push({
        name: $localize`Cadastros`,
        systemModules: cadastrosModules,
      });
    }
  }
}
