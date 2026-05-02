import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BaseComponent } from '../../../base/base.component';
import { IftaLabelModule } from 'primeng/iftalabel';
import { InputNumberModule } from 'primeng/inputnumber';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageService } from '../../../base/messages/messages.service';
import { LancamentoFinanceiroService } from '../lancamento-financeiro.service';
import { LancamentoFinanceiroDTO } from '../model/lancamento-financeiro-dto';
import { LancamentoFinanceiroProcedimentoDTO } from '../model/lancamento-financeiro-procedimento-dto';
import {
  LancamentoFinanceiroSituacao,
  lancamentoFinanceiroSituacaoLabel,
} from '../model/lancamento-financeiro-situacao.enum';
import {
  LancamentoFinanceiroStatusFinanceiro,
  lancamentoFinanceiroStatusFinanceiroLabel,
} from '../model/lancamento-financeiro-status-financeiro.enum';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { DialogService } from '../../../base/dialog/dialog.service';
import { DialogResult } from '../../../base/dialog/dialog.model';
import { EntityFieldComponent } from '../../../base/entity-field/entity-field.component';
import { EntitySearchConfig } from '../../../base/entity-search/entity-search.model';
import { ProcedimentoService } from '../../procedimento/procedimento.service';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { ProcedimentoGridDTO } from '../../procedimento/model/procedimento-grid-dto';
import { ConvenioTipoCobranca } from '../../convenio/model/convenio-tipo-cobranca.enum';

@Component({
  selector: 'gi-lancamento-financeiro-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    FormsModule,
    InputNumberModule,
    TableModule,
    ButtonModule,
    TextareaModule,
    ProgressSpinnerModule,
    EntityFieldComponent,
  ],
  providers: [LancamentoFinanceiroService, ProcedimentoService],
  templateUrl: './lancamento-financeiro-detalhe.component.html',
  styleUrl: './lancamento-financeiro-detalhe.component.css',
})
export class LancamentoFinanceiroDetalheComponent implements OnInit {
  @Input() detailId: string | null = null;
  @Output() closeDetail = new EventEmitter<string>();

  lancamento: LancamentoFinanceiroDTO | null = null;
  procedimentos: LancamentoFinanceiroProcedimentoDTO[] = [];
  observacoes: string | undefined = undefined;
  titulo = $localize`Lançamento Financeiro`;
  toolbarActions: ToolbarActionModel[] = [];

  readonly addProcedimentoLabel = $localize`Adicionar Procedimento`;
  readonly pagarLabel    = $localize`Pagar`;
  readonly fecharLabel   = $localize`Fechar para Faturamento`;
  readonly cancelarLabel = $localize`Cancelar Lançamento`;

  private service          = inject(LancamentoFinanceiroService);
  private messages         = inject(MessageService);
  private dialogService    = inject(DialogService);
  private procedimentoService = inject(ProcedimentoService);

  readonly procedimentoSearchConfig: EntitySearchConfig<ProcedimentoDTO> = {
    service: this.procedimentoService,
    searchFields: [
      { key: 'codigo', label: $localize`Código` },
      { key: 'descricao', label: $localize`Descrição` },
    ],
    resultFields: [
      { key: 'codigo', label: $localize`Código` },
      { key: 'descricao', label: $localize`Descrição` },
    ],
  };

  ngOnInit(): void {
    this.createToolbarActions();
    if (this.detailId && this.detailId !== 'add') {
      this.carregarLancamento(this.detailId);
    }
  }

  private createToolbarActions(): void {
    this.toolbarActions = [
      {
        icon: 'save',
        title: $localize`Salvar`,
        action: () => this.salvar(),
        shortcut: 'alt.s',
      },
    ];
  }

  private carregarLancamento(id: string): void {
    this.service.findById(id).subscribe((response) => {
      if (response.body) {
        this.lancamento = response.body;
        this.procedimentos = [...(this.lancamento.procedimentos ?? [])];
        this.observacoes = this.lancamento.observacoes;
        this.titulo = $localize`Lançamento Financeiro` +
          (this.lancamento.atendimentoNumero
            ? ` — Atend. #${this.lancamento.atendimentoNumero}`
            : '');
      }
    });
  }

  salvar(): void {
    if (!this.lancamento?.id) return;
    const dto: LancamentoFinanceiroDTO = {
      ...this.lancamento,
      procedimentos: this.procedimentos,
      observacoes: this.observacoes,
    };
    this.service.save(dto, {
      onSuccess: () => {
        this.messages.sucesso($localize`Lançamento salvo com sucesso.`);
        this.carregarLancamento(this.lancamento!.id!);
      },
    });
  }

  pagar(): void {
    if (!this.lancamento?.id) return;
    this.dialogService
      .showYesNo($localize`Confirmar Pagamento`, $localize`Deseja marcar este lançamento como pago?`)
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.pagar(this.lancamento!.id!).subscribe({
            next: () => {
              this.messages.sucesso($localize`Lançamento marcado como pago.`);
              this.carregarLancamento(this.lancamento!.id!);
            },
          });
        }
      });
  }

  fechar(): void {
    if (!this.lancamento?.id) return;
    this.dialogService
      .showYesNo(
        $localize`Fechar Lançamento`,
        $localize`Deseja fechar este lançamento para faturamento ao convênio?`
      )
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.fechar(this.lancamento!.id!).subscribe({
            next: () => {
              this.messages.sucesso($localize`Lançamento fechado.`);
              this.carregarLancamento(this.lancamento!.id!);
            },
          });
        }
      });
  }

  cancelarLancamento(): void {
    if (!this.lancamento?.id) return;
    this.dialogService
      .showYesNo(
        $localize`Confirmar Cancelamento`,
        $localize`Deseja cancelar este lançamento? Esta ação não pode ser desfeita.`
      )
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.cancelar(this.lancamento!.id!).subscribe({
            next: () => {
              this.messages.sucesso($localize`Lançamento cancelado.`);
              this.closeDetail.emit(this.lancamento!.id!);
            },
          });
        }
      });
  }

  onProcedimentoSelected(entity: unknown): void {
    const proc = entity as ProcedimentoGridDTO;
    const novo = new LancamentoFinanceiroProcedimentoDTO();
    novo.procedimentoId = proc.id;
    novo.procedimentoCodigo = proc.codigo;
    novo.procedimentoDescricao = proc.descricao;
    novo.valor = 0;
    this.procedimentos = [...this.procedimentos, novo];
  }

  removerProcedimento(index: number): void {
    this.procedimentos = this.procedimentos.filter((_, i) => i !== index);
    this.recalcularTotal();
  }

  recalcularTotal(): void {
    if (!this.lancamento) return;
    const total = this.procedimentos.reduce((acc, p) => acc + (p.valor ?? 0), 0);
    this.lancamento = { ...this.lancamento, valorTotal: total };
  }

  isAberto(): boolean {
    return this.lancamento?.situacao === LancamentoFinanceiroSituacao.ABERTO;
  }

  isPagoNoAto(): boolean {
    return (
      !this.lancamento?.convenioTipoCobranca ||
      this.lancamento.convenioTipoCobranca === ConvenioTipoCobranca.PAGO_NO_ATO
    );
  }

  getSituacaoClass(situacao?: LancamentoFinanceiroSituacao | string): string {
    switch (situacao) {
      case LancamentoFinanceiroSituacao.FECHADO:   return 'status-badge situacao-fechado';
      case LancamentoFinanceiroSituacao.CANCELADO: return 'status-badge situacao-cancelado';
      default:                                      return 'status-badge situacao-aberto';
    }
  }

  getSituacaoLabel(situacao?: LancamentoFinanceiroSituacao | string): string {
    return lancamentoFinanceiroSituacaoLabel(situacao ?? '');
  }

  getStatusFinanceiroClass(status?: LancamentoFinanceiroStatusFinanceiro | string): string {
    switch (status) {
      case LancamentoFinanceiroStatusFinanceiro.PAGO:     return 'status-badge status-pago';
      case LancamentoFinanceiroStatusFinanceiro.FATURADO: return 'status-badge status-faturado';
      default:                                             return 'status-badge status-pendente';
    }
  }

  getStatusFinanceiroLabel(status?: LancamentoFinanceiroStatusFinanceiro | string): string {
    return lancamentoFinanceiroStatusFinanceiroLabel(status ?? '');
  }

  formatarValor(valor?: number): string {
    if (valor == null) return 'R$ 0,00';
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  formatarData(data?: string): string {
    if (!data) return '';
    return new Date(data).toLocaleDateString('pt-BR');
  }

  goBackFn(): void {
    this.closeDetail.emit('');
  }
}
