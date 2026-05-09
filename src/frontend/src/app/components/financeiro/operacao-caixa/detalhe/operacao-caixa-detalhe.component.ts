import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { TextareaModule } from 'primeng/textarea';
import { TagModule } from 'primeng/tag';
import { IftaLabelModule } from 'primeng/iftalabel';
import { SelectModule } from 'primeng/select';

import { BaseComponent } from '../../../base/base.component';
import { MessageService } from '../../../base/messages/messages.service';
import { TableComponent } from '../../../base/table/table.component';
import { ColumnModel } from '../../../base/table/column.model';
import { AberturaCaixaService } from '../abertura-caixa.service';
import { LancamentoFinanceiroService } from '../../../atendimento/lancamento/lancamento-financeiro.service';
import { CaixaComStatusDTO } from '../model/caixa-com-status-dto';
import { MovimentacaoCaixaDTO } from '../model/movimentacao-caixa-dto';
import { StatusAberturaCaixa } from '../model/status-abertura-caixa.enum';
import { FORMA_PAGAMENTO_OPTIONS, formaPagamentoLabel } from '../model/forma-pagamento.enum';
import { LancamentoFinanceiroGridDTO } from '../../../atendimento/lancamento/model/lancamento-financeiro-grid-dto';
import {
  LancamentoFinanceiroStatusFinanceiro,
  lancamentoFinanceiroStatusFinanceiroLabel,
} from '../../../atendimento/lancamento/model/lancamento-financeiro-status-financeiro.enum';

@Component({
  selector: 'gi-operacao-caixa-detalhe',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    BaseComponent,
    ButtonModule,
    CardModule,
    DialogModule,
    InputNumberModule,
    TextareaModule,
    TagModule,
    IftaLabelModule,
    SelectModule,
    TableComponent,
  ],
  templateUrl: './operacao-caixa-detalhe.component.html',
  styleUrl: './operacao-caixa-detalhe.component.css',
  providers: [AberturaCaixaService, LancamentoFinanceiroService],
})
export class OperacaoCaixaDetalheComponent implements OnInit {
  titulo = $localize`Operação de Caixa`;
  caixa: CaixaComStatusDTO | null = null;
  lancamentoOptions: { id: string; label: string }[] = [];
  movimentacoes: MovimentacaoCaixaDTO[] = [];
  loading = false;

  showAbrirDialog = false;
  showFecharDialog = false;
  valorAbertura: number | null = null;
  valorConferencia: number | null = null;
  obsFechar = '';

  lancamentoSelecionadoId: string | null = null;
  valorReceber: number | null = null;
  formaPagamento: string | null = null;
  obsReceber = '';

  readonly StatusAberturaCaixa = StatusAberturaCaixa;
  readonly formasPagamento = FORMA_PAGAMENTO_OPTIONS;

  movimentacaoColumns: ColumnModel<MovimentacaoCaixaDTO>[] = [
    {
      name: 'dataHora',
      label: $localize`Data/Hora`,
      getValue: (m) => new Date(m.dataHora).toLocaleString('pt-BR'),
    },
    {
      name: 'formaPagamento',
      label: $localize`Forma`,
      getValue: (m) => m.formaPagamentoDescricao || formaPagamentoLabel(m.formaPagamento),
    },
    {
      name: 'valor',
      label: $localize`Valor`,
      getValue: (m) => m.valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }),
    },
    {
      name: 'observacoes',
      label: $localize`Observações`,
      getValue: (m) => m.observacoes ?? '—',
    },
  ];

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(AberturaCaixaService);
  private lancamentoService = inject(LancamentoFinanceiroService);
  private messages = inject(MessageService);

  ngOnInit(): void {
    const caixaId = this.route.snapshot.paramMap.get('caixaId');
    if (caixaId) {
      this.carregarStatus(caixaId);
    }
  }

  get isAberto(): boolean {
    return this.caixa?.statusSessao === StatusAberturaCaixa.ABERTO;
  }

  get aberturaCaixaId(): string | null {
    return this.caixa?.aberturaCaixaId ?? null;
  }

  carregarStatus(caixaId: string): void {
    this.loading = true;
    this.service.statusPorCaixa(caixaId).subscribe({
      next: (data) => {
        this.caixa = data;
        this.titulo = data.caixaNome;
        this.loading = false;
        if (this.isAberto && this.aberturaCaixaId) {
          this.carregarDadosSessao(this.aberturaCaixaId);
        }
      },
      error: () => { this.loading = false; },
    });
  }

  carregarDadosSessao(aberturaCaixaId: string): void {
    this.service.listarLancamentosPendentes(aberturaCaixaId).subscribe((list) => {
      this.lancamentoOptions = list.map(l => ({ id: l.id as string, label: this.getLancamentoLabel(l) }));
    });
    this.service.listarMovimentacoes(aberturaCaixaId).subscribe((list) => {
      this.movimentacoes = list;
    });
  }

  confirmarAbertura(): void {
    if (!this.caixa) return;
    this.service.abrir(this.caixa.caixaId, this.valorAbertura ?? 0).subscribe({
      next: () => {
        this.messages.sucesso($localize`Caixa aberto com sucesso.`);
        this.showAbrirDialog = false;
        this.carregarStatus(this.caixa!.caixaId);
      },
    });
  }

  confirmarFechamento(): void {
    if (!this.aberturaCaixaId) return;
    if (this.valorConferencia === null) {
      this.messages.erro($localize`Informe o valor de conferência.`);
      return;
    }
    this.service.fechar(this.aberturaCaixaId, this.valorConferencia, this.obsFechar).subscribe({
      next: () => {
        this.messages.sucesso($localize`Caixa fechado com sucesso.`);
        this.showFecharDialog = false;
        this.carregarStatus(this.caixa!.caixaId);
      },
    });
  }

  receber(): void {
    if (!this.lancamentoSelecionadoId || !this.aberturaCaixaId) {
      this.messages.erro($localize`Selecione um lançamento.`);
      return;
    }
    if (!this.valorReceber || this.valorReceber <= 0) {
      this.messages.erro($localize`Informe um valor válido.`);
      return;
    }
    if (!this.formaPagamento) {
      this.messages.erro($localize`Selecione a forma de pagamento.`);
      return;
    }

    this.lancamentoService.receber(this.lancamentoSelecionadoId, {
      aberturaCaixaId: this.aberturaCaixaId,
      valorRecebido: this.valorReceber,
      formaPagamento: this.formaPagamento,
      observacoes: this.obsReceber || undefined,
    }).subscribe({
      next: () => {
        this.messages.sucesso($localize`Recebimento registrado.`);
        this.resetFormReceber();
        this.carregarDadosSessao(this.aberturaCaixaId!);
      },
      error: (e) => {
        const msgs = e.error?.messages;
        this.messages.erro(msgs?.length ? msgs : $localize`Erro ao registrar recebimento.`);
      },
    });
  }

  private resetFormReceber(): void {
    this.lancamentoSelecionadoId = null;
    this.valorReceber = null;
    this.formaPagamento = null;
    this.obsReceber = '';
  }

  getSeverity(status: StatusAberturaCaixa | null): 'success' | 'danger' | 'secondary' {
    if (status === StatusAberturaCaixa.ABERTO) return 'success';
    if (status === StatusAberturaCaixa.FECHADO) return 'danger';
    return 'secondary';
  }

  getStatusLabel(status: StatusAberturaCaixa | null): string {
    if (status === StatusAberturaCaixa.ABERTO) return $localize`Aberto`;
    if (status === StatusAberturaCaixa.FECHADO) return $localize`Fechado`;
    return $localize`Sem sessão`;
  }

  getLancamentoLabel(l: LancamentoFinanceiroGridDTO): string {
    const num = l.atendimentoNumero ? `#${l.atendimentoNumero} ` : '';
    const paciente = l.pacienteNome ?? '';
    const valor = l.valorTotal?.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) ?? '';
    const status = lancamentoFinanceiroStatusFinanceiroLabel(l.statusFinanceiro ?? '');
    return `${num}${paciente} — ${valor} (${status})`;
  }

  goBackFn(): void {
    this.router.navigate(['/financeiro/operacao-caixa']);
  }
}
