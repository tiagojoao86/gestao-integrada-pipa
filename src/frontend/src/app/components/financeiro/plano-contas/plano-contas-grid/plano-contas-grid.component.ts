import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { PlanoContasGridDTO } from '../model/plano-contas-grid-dto';
import { PlanoContasService } from '../plano-contas.service';

import { AuthService } from '../../../base/auth/auth-service';
import { TableComponent } from '../../../base/table/table.component';
import { ColumnModel } from '../../../base/table/column.model';
import { ActionModel } from '../../../base/table/action.model';
import { PaginationEvent } from '../../../base/pagination/pagination-event.model';
import { PaginationComponent } from '../../../base/pagination/pagination.component';
import {
  FilterProperty,
  FilterComponent,
  FilterType,
} from '../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';
import { Order, PageRequest } from '../../../base/model/page-request';
import { TipoPlanoContas } from '../model/tipo-plano-contas.enum';
import {
  AuditInfoComponent,
  AuditInfoData,
} from '../../../base/audit-info/audit-info.component';
import { Response } from '../../../base/model/response';

@Component({
  selector: 'gi-plano-contas-grid',
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
    AuditInfoComponent,
  ],
  providers: [PlanoContasService],
  templateUrl: './plano-contas-grid.component.html',
  styleUrl: './plano-contas-grid.component.css',
})
export class PlanoContasGridComponent {
  titulo: string = $localize`:@@planoContas.title:Planos de Contas`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;
  showDeleted = false;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  planoContasList: PlanoContasGridDTO[] = [];

  authService = inject(AuthService);
  service = inject(PlanoContasService);

  columns: ColumnModel<PlanoContasGridDTO>[] = [
    {
      name: 'codigo',
      label: $localize`:@@planoContas.codigo:Código`,
      getValue: (element: PlanoContasGridDTO) => {
        return element.codigo;
      },
    },
    {
      name: 'descricao',
      label: $localize`:@@planoContas.descricao:Descrição`,
      getValue: (element: PlanoContasGridDTO) => {
        return element.descricao;
      },
    },
    {
      name: 'tipo',
      label: $localize`:@@planoContas.tipo:Tipo`,
      getValue: (element: PlanoContasGridDTO) => {
        return element.tipo;
      },
    },
    {
      name: 'planoPaiDescricao',
      label: $localize`:@@planoContas.planoPai:Plano Pai`,
      getValue: (element: PlanoContasGridDTO) => {
        if (element.planoPaiCodigo && element.planoPaiDescricao) {
          return `${element.planoPaiCodigo} - ${element.planoPaiDescricao}`;
        }
        return '-';
      },
    },
    {
      name: 'unidadeNegocioCodigo',
      label: $localize`:@@planoContas.unidadeNegocio:Unidade`,
      getValue: (element: PlanoContasGridDTO) => {
        return element.unidadeNegocioCodigo || '-';
      },
    },
    {
      name: 'analitico',
      label: $localize`:@@planoContas.analitico:Analítico`,
      getValue: (element: PlanoContasGridDTO) => {
        return element.analitico
          ? $localize`:@@common.yes:Sim`
          : $localize`:@@common.no:Não`;
      },
    },
    {
      name: 'ativo',
      label: $localize`:@@planoContas.ativo:Ativo`,
      getValue: (element: PlanoContasGridDTO) => {
        return element.ativo
          ? $localize`:@@common.yes:Sim`
          : $localize`:@@common.no:Não`;
      },
    },
  ];

  filtros: FilterProperty[] = [
    {
      property: 'codigo',
      label: $localize`:@@planoContas.codigo:Código`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'descricao',
      label: $localize`:@@planoContas.descricao:Descrição`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'tipo',
      label: $localize`:@@planoContas.tipo:Tipo`,
      filterType: FilterType.SELECT,
      options: TipoPlanoContas.getList().map((tipo) => ({
        key: tipo.getKey(),
        label: tipo.getLabel(),
      })),
    },
    {
      property: 'analitico',
      label: $localize`:@@planoContas.analitico:Analítico`,
      filterType: FilterType.SELECT,
      options: [
        { key: 'true', label: $localize`:@@common.yes:Sim` },
        { key: 'false', label: $localize`:@@common.no:Não` },
      ],
    },
    {
      property: 'ativo',
      label: $localize`:@@planoContas.ativo:Ativo`,
      filterType: FilterType.SELECT,
      options: [
        { key: 'true', label: $localize`:@@common.yes:Sim` },
        { key: 'false', label: $localize`:@@common.no:Não` },
      ],
    },
  ];

  toolbarActions: ToolbarActionModel[] = [];

  tableActions: ActionModel<PlanoContasGridDTO>[] = [];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  constructor() {
    const canView = this.authService.hasAuthorityVisualizarToModulo(
      'CADASTRO_PLANO_CONTAS'
    );
    const canEdit = this.authService.hasAuthorityEditarToModulo(
      'CADASTRO_PLANO_CONTAS'
    );
    const canDelete = this.authService.hasAuthorityDeletarToModulo(
      'CADASTRO_PLANO_CONTAS'
    );

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (element: PlanoContasGridDTO) =>
          this.openDetail.emit(element.id),
      });
    }

    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (element: PlanoContasGridDTO) => {
          this.service
            .delete(element.id)
            .subscribe(() => this.listarPlanoContas());
        },
      });
    }

    const canAudit = this.authService.hasAuthorityAuditarToModulo('CADASTRO_PLANO_CONTAS');

    if (canAudit) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: 'Visualizar auditoria',
        action: (element: PlanoContasGridDTO) => this.loadAuditInfo(element.id),
      });
    }

    this.toolbarActions = [
      {
        action: () => {
          this.refreshList();
        },
        icon: 'refresh',
        title: $localize`Atualizar` + ' (alt + r)',
        shortcut: 'alt.r',
      },
    ];

    if (canEdit) {
      this.toolbarActions.push({
        action: () => {
          this.openDetail.emit('add');
        },
        icon: 'add',
        title: $localize`Adicionar` + ' (alt + a)',
        shortcut: 'alt.a',
      });
    }

    this.toolbarActions.push({
      action: () => {
        this.toggleShowFilters();
      },
      icon: 'search',
      title: $localize`Pesquisar` + ' (alt + p)',
      value: '0',
      shortcut: 'alt.p',
    });

    if (this.authService.hasAuthorityAuditarToModulo('CADASTRO_PLANO_CONTAS')) {
      this.toolbarActions.unshift({
        action: () => {
          this.toggleShowDeleted();
        },
        icon: 'visibility',
        title: $localize`Mostrar excluídos` + ' (alt + d)',
        shortcut: 'alt.d',
      });
    }

    this.listarPlanoContas();
  }

  listarPlanoContas() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.planoContasList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listarPlanoContas();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;

    this.listarPlanoContas();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.request.filter.showDeleted = this.showDeleted;
    this.listarPlanoContas();
    this.updateFilterBadge(filter);
  }

  closeFilter() {
    this.toggleShowFilters();
  }

  updateFilterBadge(filter: FilterDTO) {
    const acao = this.toolbarActions.filter((it) => it.icon === 'search');
    if (acao.length > 0) {
      if (filter) {
        acao[0].value = filter.items.length + '';
        return;
      }
      acao[0].value = '0';
    }
  }

  toggleShowFilters() {
    this.hideFilters = !this.hideFilters;
  }

  toggleShowDeleted() {
    this.showDeleted = !this.showDeleted;
    this.request.filter.showDeleted = this.showDeleted;
    this.updateShowDeletedIcon();
    this.listarPlanoContas();
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
    this.listarPlanoContas();
  }

  loadAuditInfo(id: string) {
    this.service
      .getAuditInfo(id)
      .subscribe((response: Response<AuditInfoData>) => {
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
