import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { LancamentoFinanceiroService } from '../lancamento-financeiro.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { LancamentoFinanceiroGridDTO } from '../model/lancamento-financeiro-grid-dto';
import {
  LancamentoFinanceiroSituacao,
  lancamentoFinanceiroSituacaoLabel,
} from '../model/lancamento-financeiro-situacao.enum';
import {
  LancamentoFinanceiroStatusFinanceiro,
  lancamentoFinanceiroStatusFinanceiroLabel,
} from '../model/lancamento-financeiro-status-financeiro.enum';
import { AuthService } from '../../../base/auth/auth-service';
import { DialogService } from '../../../base/dialog/dialog.service';
import { DialogResult } from '../../../base/dialog/dialog.model';
import { TableComponent } from '../../../base/table/table.component';
import { ColumnModel } from '../../../base/table/column.model';
import { ActionModel } from '../../../base/table/action.model';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { PaginationEvent } from '../../../base/pagination/pagination-event.model';
import { PaginationComponent } from '../../../base/pagination/pagination.component';
import {
  FilterProperty,
  FilterComponent,
  FilterType,
} from '../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import {
  AuditInfoComponent,
  AuditInfoData,
} from '../../../base/audit-info/audit-info.component';
import { Response } from '../../../base/model/response';

@Component({
  selector: 'gi-lancamento-financeiro-grid',
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
    AuditInfoComponent,
  ],
  providers: [LancamentoFinanceiroService],
  templateUrl: './lancamento-financeiro-grid.component.html',
  styleUrl: './lancamento-financeiro-grid.component.css',
})
export class LancamentoFinanceiroGridComponent {
  titulo: string = $localize`Lançamentos Financeiros`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;
  showDeleted = false;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  rows: LancamentoFinanceiroGridDTO[] = [];

  columns: ColumnModel<LancamentoFinanceiroGridDTO>[] = [
    {
      name: 'atendimentoNumero',
      label: $localize`Nº Atend.`,
      getValue: (e) => e.atendimentoNumero != null ? `#${e.atendimentoNumero}` : '',
    },
    {
      name: 'dataAtendimento',
      label: $localize`Data`,
      getValue: (e) => e.dataAtendimento
        ? new Date(e.dataAtendimento).toLocaleDateString('pt-BR') : '',
    },
    {
      name: 'pacienteNome',
      label: $localize`Paciente`,
      getValue: (e) => e.pacienteNome ?? '',
    },
    {
      name: 'convenioNome',
      label: $localize`Convênio`,
      getValue: (e) => e.convenioNome ?? $localize`Particular`,
    },
    {
      name: 'procedimentosCount',
      label: $localize`Procedimentos`,
      getValue: (e) => `${e.procedimentosCount ?? 0}`,
    },
    {
      name: 'valorTotal',
      label: $localize`Valor Total`,
      getValue: (e) => e.valorTotal != null
        ? e.valorTotal.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : 'R$ 0,00',
    },
    {
      name: 'situacao',
      label: $localize`Situação`,
      isHtml: true,
      getValue: (e) =>
        `<span class="${this.getSituacaoClass(e.situacao)}">`
        + `${lancamentoFinanceiroSituacaoLabel(e.situacao ?? '')}</span>`,
    },
    {
      name: 'statusFinanceiro',
      label: $localize`Status`,
      isHtml: true,
      getValue: (e) =>
        `<span class="${this.getStatusFinanceiroClass(e.statusFinanceiro)}">`
        + `${lancamentoFinanceiroStatusFinanceiroLabel(e.statusFinanceiro ?? '')}</span>`,
    },
  ];

  tableActions: ActionModel<LancamentoFinanceiroGridDTO>[] = [];
  toolbarActions: ToolbarActionModel[] = [];

  filtros: FilterProperty[] = [
    { property: 'dataAtendimento', label: $localize`Data`, filterType: FilterType.DATE },
    { property: 'pacienteNome', label: $localize`Paciente`, filterType: FilterType.TEXT },
    { property: 'situacao', label: $localize`Situação`, filterType: FilterType.TEXT },
    { property: 'statusFinanceiro', label: $localize`Status`, filterType: FilterType.TEXT },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service = inject(LancamentoFinanceiroService);
  private auth = inject(AuthService);
  private dialogService = inject(DialogService);

  constructor() {
    const canView   = this.auth.hasAuthorityVisualizarToModulo(SystemModuleKey.LANCAMENTO_FINANCEIRO);
    const canDelete = this.auth.hasAuthorityDeletarToModulo(SystemModuleKey.LANCAMENTO_FINANCEIRO);
    const canAudit  = this.auth.hasAuthorityAuditarToModulo(SystemModuleKey.LANCAMENTO_FINANCEIRO);

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (e: LancamentoFinanceiroGridDTO) => this.openDetail.emit(e.id!),
      });
    }

    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (e: LancamentoFinanceiroGridDTO) => {
          this.dialogService
            .showYesNo(
              $localize`Confirmar Exclusão`,
              $localize`Deseja realmente excluir este lançamento financeiro?`
            )
            .subscribe((result) => {
              if (result === DialogResult.YES) {
                this.service.delete(e.id!).subscribe(() => this.listar());
              }
            });
        },
      });
    }

    if (canAudit) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: $localize`Visualizar auditoria`,
        action: (e: LancamentoFinanceiroGridDTO) => this.loadAuditInfo(e.id!),
      });
    }

    this.toolbarActions = [
      {
        action: () => this.refreshList(),
        icon: 'refresh',
        title: $localize`Atualizar` + ' (alt + r)',
        shortcut: 'alt.r',
      },
      {
        action: () => this.toggleShowFilters(),
        icon: 'search',
        title: $localize`Pesquisar` + ' (alt + p)',
        value: '0',
        shortcut: 'alt.p',
      },
    ];

    if (canAudit) {
      this.toolbarActions.unshift({
        action: () => this.toggleShowDeleted(),
        icon: 'visibility',
        title: $localize`Mostrar excluídos` + ' (alt + d)',
        shortcut: 'alt.d',
      });
    }

    this.listar();
  }

  getSituacaoClass(situacao?: LancamentoFinanceiroSituacao | string): string {
    switch (situacao) {
      case LancamentoFinanceiroSituacao.FECHADO:   return 'status-badge situacao-fechado';
      case LancamentoFinanceiroSituacao.CANCELADO: return 'status-badge situacao-cancelado';
      default:                                      return 'status-badge situacao-aberto';
    }
  }

  getStatusFinanceiroClass(status?: LancamentoFinanceiroStatusFinanceiro | string): string {
    switch (status) {
      case LancamentoFinanceiroStatusFinanceiro.PAGO:     return 'status-badge status-pago';
      case LancamentoFinanceiroStatusFinanceiro.FATURADO: return 'status-badge status-faturado';
      default:                                             return 'status-badge status-pendente';
    }
  }

  listar() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.rows = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listar();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listar();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.request.filter.showDeleted = this.showDeleted;
    this.listar();
    this.updateFilterBadge(filter);
  }

  closeFilter() {
    this.toggleShowFilters();
  }

  updateFilterBadge(filter: FilterDTO) {
    const acao = this.toolbarActions.filter((it) => it.icon === 'search');
    if (acao.length > 0) {
      acao[0].value = filter?.items ? filter.items.length + '' : '0';
    }
  }

  toggleShowFilters() {
    this.hideFilters = !this.hideFilters;
  }

  toggleShowDeleted() {
    this.showDeleted = !this.showDeleted;
    this.request.filter.showDeleted = this.showDeleted;
    const acao = this.toolbarActions.filter(
      (it) => it.icon === 'visibility' || it.icon === 'visibility_off'
    );
    if (acao.length > 0) {
      acao[0].icon = this.showDeleted ? 'visibility_off' : 'visibility';
      acao[0].title =
        (this.showDeleted ? $localize`Ocultar excluídos` : $localize`Mostrar excluídos`) +
        ' (alt + d)';
    }
    this.listar();
  }

  refreshList() {
    this.listar();
  }

  loadAuditInfo(id: string) {
    this.service.getAuditInfo(id).subscribe((response: Response<AuditInfoData>) => {
      if (response.body) {
        this.auditInfoData = response.body;
        this.showAuditInfo = true;
      }
    });
  }

  closeAuditInfo() {
    this.showAuditInfo = false;
    this.auditInfoData = null;
  }
}
