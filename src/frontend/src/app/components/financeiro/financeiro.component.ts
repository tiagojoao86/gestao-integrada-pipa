import { Component, OnInit, inject } from '@angular/core';
import { BaseComponent } from '../base/base.component';
import { CommonModule } from '@angular/common';
import {
  GrupoRecurso,
  RecursoGrupoComponent,
} from '../base/menu/recurso-grupo/recurso-grupo.component';
import { AuthService } from '../base/auth/auth-service';
import { Recurso } from '../base/menu/recurso/recurso.component';

@Component({
  selector: 'gi-financeiro',
  imports: [BaseComponent, CommonModule, RecursoGrupoComponent],
  templateUrl: './financeiro.component.html',
  styleUrl: './financeiro.component.css',
  standalone: true,
})
export class FinanceiroComponent implements OnInit {
  titulo: string = $localize`Financeiro`;
  recursos: GrupoRecurso[] = [];

  private authService: AuthService = inject(AuthService);

  ngOnInit(): void {
    const recursosCadastros: Recurso[] = [];

    if (this.authService.hasAuthorityListarToModulo('CADASTRO_PLANO_CONTAS')) {
      recursosCadastros.push({
        nome: $localize`Plano de Contas`,
        icone: 'account_tree',
        url: '/financeiro/plano-contas',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo('CADASTRO_CONTA_BANCARIA')
    ) {
      recursosCadastros.push({
        nome: $localize`Contas Bancárias`,
        icone: 'account_balance',
        url: '/financeiro/conta-bancaria',
      });
    }

    const recursosOperacoes: Recurso[] = [];

    if (this.authService.hasAuthorityListarToModulo('FINANCEIRO_TITULO')) {
      recursosOperacoes.push({
        nome: $localize`Títulos`,
        icone: 'receipt_long',
        url: '/financeiro/titulo',
      });
    }

    if (
      this.authService.hasAuthorityListarToModulo('FINANCEIRO_MOVIMENTACAO')
    ) {
      recursosOperacoes.push({
        nome: $localize`Movimentações`,
        icone: 'payments',
        url: '/financeiro/movimentacao',
      });
    }

    if (recursosOperacoes.length > 0) {
      this.recursos.push({
        nome: $localize`Operações`,
        recursos: recursosOperacoes,
      });
    }

    if (recursosCadastros.length > 0) {
      this.recursos.push({
        nome: $localize`Cadastros`,
        recursos: recursosCadastros,
      });
    }
  }
}
