import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { CaixaGridDTO } from '../model/caixa-grid-dto';
import { CaixaService } from '../caixa.service';
import { AuthService } from '../../../base/auth/auth-service';
import { DialogService } from '../../../base/dialog/dialog.service';
import { DialogResult } from '../../../base/dialog/dialog.model';
import { TableComponent } from '../../../base/table/table.component';
import { ColumnModel } from '../../../base/table/column.model';
import { ActionModel } from '../../../base/table/action.model';
import { PaginationEvent } from '../../../base/pagination/pagination-event.model';
import { PaginationComponent } from '../../../base/pagination/pagination.component';
import { FilterProperty, FilterComponent, FilterType } from '../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';
import { Order, PageRequest } from '../../../base/model/page-request';
import { AuditInfoComponent, AuditInfoData } from '../../../base/audit-info/audit-info.component';
import { Response } from '../../../base/model/response';

@Component({
  selector: 'gi-caixa-grid',
  imports: [BaseComponent, TableComponent, PaginationComponent, FilterComponent, AuditInfoComponent],
  providers: [CaixaService],
  templateUrl: './caixa-grid.component.html',
  styleUrl: './caixa-grid.component.css',
})
export class CaixaGridComponent {
  titulo = $localize`:@@caixa.title:Cadastro de Caixas`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;
  showDeleted = false;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  caixaList: CaixaGridDTO[] = [];

  authService = inject(AuthService);
  service = inject(CaixaService);
  dialogService = inject(DialogService);

  columns: ColumnModel<CaixaGridDTO>[] = [
    {
      name: 'nome',
      label: $localize`:@@caixa.nome:Nome`,
      getValue: (e: CaixaGridDTO) => e.nome,
    },
    {
      name: 'unidadeNegocioNome',
      label: $localize`:@@caixa.unidadeNegocio:Unidade de Negócio`,
      getValue: (e: CaixaGridDTO) => e.unidadeNegocioNome ?? '-',
    },
    {
      name: 'valorPadraoAbertura',
      label: $localize`:@@caixa.valorPadraoAbertura:Valor Padrão Abertura`,
      getValue: (e: CaixaGridDTO) =>
        e.valorPadraoAbertura?.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) ?? '-',
    },
    {
      name: 'percentualParcialConfigurado',
      label: $localize`:@@caixa.pagamentoParcial:Pagamento Parcial`,
      getValue: (e: CaixaGridDTO) =>
        e.percentualParcialConfigurado
          ? $localize`:@@common.yes:Sim`
          : $localize`:@@common.no:Não`,
    },
    {
      name: 'ativo',
      label: $localize`:@@caixa.ativo:Ativo`,
      getValue: (e: CaixaGridDTO) =>
        e.ativo ? $localize`:@@common.yes:Sim` : $localize`:@@common.no:Não`,
    },
  ];

  filtros: FilterProperty[] = [
    {
      property: 'nome',
      label: $localize`:@@caixa.nome:Nome`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'ativo',
      label: $localize`:@@caixa.ativo:Ativo`,
      filterType: FilterType.SELECT,
      options: [
        { key: 'true', label: $localize`:@@common.yes:Sim` },
        { key: 'false', label: $localize`:@@common.no:Não` },
      ],
    },
  ];

  toolbarActions: ToolbarActionModel[] = [];
  tableActions: ActionModel<CaixaGridDTO>[] = [];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  constructor() {
    const canView = this.authService.hasAuthorityVisualizarToModulo('CADASTRO_CAIXA');
    const canEdit = this.authService.hasAuthorityEditarToModulo('CADASTRO_CAIXA');
    const canDelete = this.authService.hasAuthorityDeletarToModulo('CADASTRO_CAIXA');
    const canAudit = this.authService.hasAuthorityAuditarToModulo('CADASTRO_CAIXA');

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (e: CaixaGridDTO) => this.openDetail.emit(e.id),
      });
    }

    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (e: CaixaGridDTO) => {
          this.dialogService
            .showYesNo(
              $localize`Confirmar Exclusão`,
              $localize`Deseja realmente excluir o registro selecionado?`
            )
            .subscribe((result) => {
              if (result === DialogResult.YES) {
                this.service.delete(e.id).subscribe(() => this.listarCaixas());
              }
            });
        },
      });
    }

    if (canAudit) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: 'Visualizar auditoria',
        action: (e: CaixaGridDTO) => this.loadAuditInfo(e.id),
      });
    }

    this.toolbarActions = [
      {
        action: () => this.refreshList(),
        icon: 'refresh',
        title: $localize`Atualizar` + ' (alt + r)',
        shortcut: 'alt.r',
      },
    ];

    if (canEdit) {
      this.toolbarActions.push({
        action: () => this.openDetail.emit('add'),
        icon: 'add',
        title: $localize`Adicionar` + ' (alt + a)',
        shortcut: 'alt.a',
      });
    }

    this.toolbarActions.push({
      action: () => this.toggleShowFilters(),
      icon: 'search',
      title: $localize`Pesquisar` + ' (alt + p)',
      value: '0',
      shortcut: 'alt.p',
    });

    if (canAudit) {
      this.toolbarActions.unshift({
        action: () => this.toggleShowDeleted(),
        icon: 'visibility',
        title: $localize`Mostrar excluídos` + ' (alt + d)',
        shortcut: 'alt.d',
      });
    }

    this.listarCaixas();
  }

  listarCaixas() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.caixaList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listarCaixas();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listarCaixas();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.request.filter.showDeleted = this.showDeleted;
    this.listarCaixas();
    this.updateFilterBadge(filter);
  }

  closeFilter() {
    this.toggleShowFilters();
  }

  updateFilterBadge(filter: FilterDTO) {
    const acao = this.toolbarActions.filter((it) => it.icon === 'search');
    if (acao.length > 0) {
      acao[0].value = filter ? (filter.items?.length ?? 0) + '' : '0';
    }
  }

  toggleShowFilters() {
    this.hideFilters = !this.hideFilters;
  }

  toggleShowDeleted() {
    this.showDeleted = !this.showDeleted;
    this.request.filter.showDeleted = this.showDeleted;
    this.updateShowDeletedIcon();
    this.listarCaixas();
  }

  updateShowDeletedIcon() {
    const acao = this.toolbarActions.filter(
      (it) => it.icon === 'visibility' || it.icon === 'visibility_off'
    );
    if (acao.length > 0) {
      acao[0].icon = this.showDeleted ? 'visibility_off' : 'visibility';
      acao[0].title = this.showDeleted
        ? $localize`Ocultar excluídos` + ' (alt + d)'
        : $localize`Mostrar excluídos` + ' (alt + d)';
    }
  }

  refreshList() {
    this.listarCaixas();
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
