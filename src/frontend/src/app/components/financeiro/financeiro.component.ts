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
    const operacoesModules: SystemModule[] = [];

    if (
      this.authService.hasAuthorityListarToModulo(SystemModuleKey.OPERACAO_CAIXA)
    ) {
      operacoesModules.push({
        name: $localize`Caixa`,
        icon: 'point_of_sale',
        url: '/financeiro/operacao-caixa',
      });
    }

    if (operacoesModules.length > 0) {
      this.systemModuleGroups.push({
        name: $localize`Operações`,
        systemModules: operacoesModules,
      });
    }

    const movimentacoesModules: SystemModule[] = [];

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.LANCAMENTO_FINANCEIRO,
      )
    ) {
      movimentacoesModules.push({
        name: $localize`Lançamentos Financeiros`,
        icon: 'assignment',
        url: '/atendimento/lancamento',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.FINANCEIRO_TITULO,
      )
    ) {
      movimentacoesModules.push({
        name: $localize`Títulos`,
        icon: 'receipt_long',
        url: '/financeiro/titulo',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.FINANCEIRO_MOVIMENTACAO_FINANCEIRA,
      )
    ) {
      movimentacoesModules.push({
        name: $localize`Movimentações Financeiras`,
        icon: 'payments',
        url: '/financeiro/movimentacao',
      });
    }

    if (movimentacoesModules.length > 0) {
      this.systemModuleGroups.push({
        name: $localize`Movimentações`,
        systemModules: movimentacoesModules,
      });
    }
  }
}
