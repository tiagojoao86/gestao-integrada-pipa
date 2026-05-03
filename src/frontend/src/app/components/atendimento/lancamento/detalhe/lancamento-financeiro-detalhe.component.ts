import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { BaseComponent } from '../../../base/base.component';
import { IftaLabelModule } from 'primeng/iftalabel';
import { ButtonModule } from 'primeng/button';
import { TableComponent } from '../../../base/table/table.component';
import { ColumnModel } from '../../../base/table/column.model';
import { ActionModel } from '../../../base/table/action.model';
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
    ButtonModule,
    TextareaModule,
    ProgressSpinnerModule,
    EntityFieldComponent,
    TableComponent,
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
  procedimentoColumns: ColumnModel<LancamentoFinanceiroProcedimentoDTO>[] = [];
  procedimentoActions: ActionModel<LancamentoFinanceiroProcedimentoDTO>[] = [];

  readonly addProcedimentoLabel = $localize`Adicionar Procedimento`;
  readonly fecharPagamentoLabel = $localize`Fechar para Pagamento`;
  readonly fecharFaturamentoLabel = $localize`Fechar para Faturamento`;
  readonly cancelarLabel = $localize`Cancelar Lançamento`;

  private service = inject(LancamentoFinanceiroService);
  private messages = inject(MessageService);
  private dialogService = inject(DialogService);
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
    this.initProcedimentoColumns();
    this.createToolbarActions();
    if (this.detailId && this.detailId !== 'add') {
      this.carregarLancamento(this.detailId);
    }
  }

  private initProcedimentoColumns(): void {
    this.procedimentoColumns = [
      {
        name: 'procedimento',
        label: $localize`Procedimento`,
        getValue: (p) => p.procedimentoDescricao || p.procedimentoCodigo || '—',
      },
      {
        name: 'convenio',
        label: $localize`Convênio`,
        getValue: (p) => p.convenioNome ?? 'Particular',
      },
      {
        name: 'valor',
        label: $localize`Valor (R$)`,
        getValue: (p) => this.formatarValor(p.valor),
      },
    ];
  }

  private setupProcedimentoActions(): void {
    if (this.isAberto()) {
      this.procedimentoActions = [
        {
          icon: 'delete',
          title: $localize`Remover`,
          iconType: 'material-symbols-outlined',
          action: (proc) => this.removerProcedimento(proc),
        },
      ];
    } else {
      this.procedimentoActions = [];
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
        this.titulo =
          $localize`Lançamento Financeiro` +
          (this.lancamento.atendimentoNumero
            ? ` — Atend. #${this.lancamento.atendimentoNumero}`
            : '');
        this.setupProcedimentoActions();
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

  fecharParaPagamento(): void {
    if (!this.lancamento?.id) return;
    this.dialogService
      .showYesNo(
        $localize`Fechar para Pagamento`,
        $localize`Deseja fechar este lançamento e gerar o título a receber?`,
      )
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.fecharParaPagamento(this.lancamento!.id!).subscribe({
            next: () => {
              this.messages.sucesso($localize`Lançamento fechado.`);
              this.carregarLancamento(this.lancamento!.id!);
            },
            error: (e: HttpErrorResponse) => this.onHttpError(e),
          });
        }
      });
  }

  fecharParaFaturamento(): void {
    if (!this.lancamento?.id) return;
    this.dialogService
      .showYesNo(
        $localize`Fechar para Faturamento`,
        $localize`Deseja fechar este lançamento para faturamento ao convênio?`,
      )
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.fecharParaFaturamento(this.lancamento!.id!).subscribe({
            next: () => {
              this.messages.sucesso($localize`Lançamento fechado.`);
              this.carregarLancamento(this.lancamento!.id!);
            },
            error: (e: HttpErrorResponse) => this.onHttpError(e),
          });
        }
      });
  }

  cancelarLancamento(): void {
    if (!this.lancamento?.id) return;
    this.dialogService
      .showYesNo(
        $localize`Confirmar Cancelamento`,
        $localize`Deseja cancelar este lançamento? Esta ação não pode ser desfeita.`,
      )
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.cancelar(this.lancamento!.id!).subscribe({
            next: () => {
              this.messages.sucesso($localize`Lançamento cancelado.`);
              this.closeDetail.emit(this.lancamento!.id!);
            },
            error: (e: HttpErrorResponse) => this.onHttpError(e),
          });
        }
      });
  }

  private onHttpError(error: HttpErrorResponse): void {
    if (error.error?.messages?.length > 0) {
      this.messages.erro(error.error.messages);
    } else {
      this.messages.erro(
        error.error?.title ?? $localize`Ocorreu um erro inesperado.`,
      );
    }
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

  removerProcedimento(proc: LancamentoFinanceiroProcedimentoDTO): void {
    this.procedimentos = this.procedimentos.filter((p) => p !== proc);
    this.recalcularTotal();
  }

  recalcularTotal(): void {
    if (!this.lancamento) return;
    const total = this.procedimentos.reduce(
      (acc, p) => acc + (p.valor ?? 0),
      0,
    );
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
      case LancamentoFinanceiroSituacao.FECHADO:
        return 'status-badge situacao-fechado';
      case LancamentoFinanceiroSituacao.CANCELADO:
        return 'status-badge situacao-cancelado';
      default:
        return 'status-badge situacao-aberto';
    }
  }

  getSituacaoLabel(situacao?: LancamentoFinanceiroSituacao | string): string {
    return lancamentoFinanceiroSituacaoLabel(situacao ?? '');
  }

  getStatusFinanceiroClass(
    status?: LancamentoFinanceiroStatusFinanceiro | string,
  ): string {
    switch (status) {
      case LancamentoFinanceiroStatusFinanceiro.PAGO:
        return 'status-badge status-pago';
      case LancamentoFinanceiroStatusFinanceiro.FATURADO:
        return 'status-badge status-faturado';
      default:
        return 'status-badge status-pendente';
    }
  }

  getStatusFinanceiroLabel(
    status?: LancamentoFinanceiroStatusFinanceiro | string,
  ): string {
    return lancamentoFinanceiroStatusFinanceiroLabel(status ?? '');
  }

  formatarValor(valor?: number): string {
    if (valor == null) return 'R$ 0,00';
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    });
  }

  formatarData(data?: string): string {
    if (!data) return '';
    return new Date(data).toLocaleDateString('pt-BR');
  }

  goBackFn(): void {
    this.closeDetail.emit('');
  }
}
