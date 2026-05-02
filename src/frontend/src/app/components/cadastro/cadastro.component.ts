import { Component, OnInit, inject } from '@angular/core';
import { BaseComponent } from '../base/base.component';

import { SystemModuleGroupComponent } from '../base/menu/system-module-group/system-module-group.component';
import { AuthService } from '../base/auth/auth-service';
import { SystemModuleGroup } from '../base/menu/system-module-group/system-module-group';
import { SystemModule } from '../base/menu/system-module/system-module';
import { SystemModuleKey } from '../base/enum/system-module-key.enum';

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
    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.CADASTRO_PESSOA
      )
    ) {
      systemModulesCadastros.push({
        name: $localize`Pessoas`,
        icon: 'groups',
        url: '/cadastro/pessoa',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.CADASTRO_UNIDADE_NEGOCIO
      )
    ) {
      systemModulesCadastros.push({
        name: $localize`Unidades de Negócio`,
        icon: 'factory',
        url: '/cadastro/unidade-negocio',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.CADASTRO_SETOR
      )
    ) {
      systemModulesCadastros.push({
        name: $localize`Setores`,
        icon: 'business',
        url: '/cadastro/setor',
      });
    }

    if (systemModulesCadastros.length > 0) {
      this.systemModules.push({
        name: $localize`Geral`,
        systemModules: systemModulesCadastros,
      });
    }
    const systemModulesGeral: SystemModule[] = [];
    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.CADASTRO_USUARIO
      )
    ) {
      systemModulesGeral.push({
        name: $localize`Usuários`,
        icon: 'person',
        url: '/cadastro/usuario',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo(
        SystemModuleKey.CADASTRO_PERFIL
      )
    ) {
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

    const atendimentoModules: SystemModule[] = [];

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.ATENDIMENTO_PROFISSIONAL)) {
      atendimentoModules.push({
        name: $localize`Profissionais`,
        icon: 'medical_services',
        url: '/atendimento/profissional',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.ATENDIMENTO_CONVENIO)) {
      atendimentoModules.push({
        name: $localize`Convênios`,
        icon: 'handshake',
        url: '/atendimento/convenio',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.ATENDIMENTO_CONVENIO_CATEGORIA)) {
      atendimentoModules.push({
        name: $localize`Categorias de Convênio`,
        icon: 'category',
        url: '/atendimento/convenio-categoria',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.ATENDIMENTO_PROCEDIMENTO)) {
      atendimentoModules.push({
        name: $localize`Procedimentos`,
        icon: 'clinical_notes',
        url: '/atendimento/procedimento',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.ATENDIMENTO_TABELA)) {
      atendimentoModules.push({
        name: $localize`Tabelas de Preços`,
        icon: 'price_change',
        url: '/atendimento/tabela',
      });
    }

    if (atendimentoModules.length > 0) {
      this.systemModules.push({
        name: $localize`Atendimento`,
        systemModules: atendimentoModules,
      });
    }

    const financeiroModules: SystemModule[] = [];

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.FINANCEIRO_CONTA_BANCARIA)) {
      financeiroModules.push({
        name: $localize`Contas Bancárias`,
        icon: 'account_balance',
        url: '/financeiro/conta-bancaria',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA)) {
      financeiroModules.push({
        name: $localize`Categorias de Título`,
        icon: 'category',
        url: '/financeiro/titulo-categoria',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.FINANCEIRO_CENTRO_CUSTO)) {
      financeiroModules.push({
        name: $localize`Centros de Custo`,
        icon: 'paid',
        url: '/financeiro/centro-custo',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.FINANCEIRO_CONDICAO_PAGAMENTO)) {
      financeiroModules.push({
        name: $localize`Condições de Pagamento`,
        icon: 'schedule',
        url: '/financeiro/condicao-pagamento',
      });
    }

    if (this.authService.hasAuthorityListarToModulo(SystemModuleKey.CADASTRO_CAIXA)) {
      financeiroModules.push({
        name: $localize`Caixas`,
        icon: 'point_of_sale',
        url: '/financeiro/caixa',
      });
    }

    if (financeiroModules.length > 0) {
      this.systemModules.push({
        name: $localize`Financeiro`,
        systemModules: financeiroModules,
      });
    }
  }
}
