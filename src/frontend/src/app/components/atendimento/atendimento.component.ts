import { Component, OnInit, inject } from '@angular/core';
import { BaseComponent } from '../base/base.component';
import { SystemModuleGroupComponent } from '../base/menu/system-module-group/system-module-group.component';
import { AuthService } from '../base/auth/auth-service';
import { SystemModuleGroup } from '../base/menu/system-module-group/system-module-group';
import { SystemModule } from '../base/menu/system-module/system-module';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-atendimento',
  standalone: true,
  imports: [BaseComponent, SystemModuleGroupComponent],
  templateUrl: './atendimento.component.html',
  styleUrl: './atendimento.component.css',
})
export class AtendimentoComponent implements OnInit {
  title: string = $localize`Atendimento`;
  systemModules: SystemModuleGroup[] = [];

  private authService: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.buildGrupoAtendimento();
    this.buildGrupoAgendamento();
    this.buildGrupoFinanceiro();
  }

  private buildGrupoAtendimento(): void {
    const modulos: SystemModule[] = [];

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.ATENDIMENTO)) {
      modulos.push({
        name: $localize`Atendimentos`,
        icon: 'event_note',
        url: '/atendimento/atendimento',
      });
    }

    if (modulos.length > 0) {
      this.systemModules.push({ name: $localize`Atendimento`, systemModules: modulos });
    }
  }

  private buildGrupoAgendamento(): void {
    const modulos: SystemModule[] = [];

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.AGENDAMENTO_AGENDA)) {
      modulos.push({
        name: $localize`Agendas`,
        icon: 'calendar_month',
        url: '/atendimento/agendamento/agenda',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.AGENDAMENTO_AGENDAMENTO)) {
      modulos.push({
        name: $localize`Agendamentos`,
        icon: 'event_available',
        url: '/atendimento/agendamento/agendamento',
      });
    }

    if (modulos.length > 0) {
      this.systemModules.push({ name: $localize`Agendamento`, systemModules: modulos });
    }
  }

  private buildGrupoFinanceiro(): void {
    const modulos: SystemModule[] = [];

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.LANCAMENTO_FINANCEIRO)) {
      modulos.push({
        name: $localize`Lançamentos Financeiros`,
        icon: 'payments',
        url: '/atendimento/lancamento',
      });
    }

    if (modulos.length > 0) {
      this.systemModules.push({ name: $localize`Financeiro`, systemModules: modulos });
    }
  }
}
