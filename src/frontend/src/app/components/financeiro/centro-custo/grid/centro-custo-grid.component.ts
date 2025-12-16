import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { CentroCustoService } from '../centro-custo.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { CentroCustoGridDTO } from '../model/centro-custo-grid-dto';
import { AuthService } from '../../../base/auth/auth-service';
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

@Component({
  selector: 'gi-centro-custo-grid',
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
  ],
  providers: [CentroCustoService],
  templateUrl: './centro-custo-grid.component.html',
  styleUrl: './centro-custo-grid.component.css',
})
export class CentroCustoGridComponent {
  titulo: string = $localize`Centros de Custo`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;

  rows: CentroCustoGridDTO[] = [];

  columns: ColumnModel<CentroCustoGridDTO>[] = [
    {
      name: 'nome',
      label: $localize`Nome`,
      getValue: (element: CentroCustoGridDTO) => element.nome,
    },
    {
      name: 'unidadeNegocioCodigo',
      label: $localize`Unidade Negócio`,
      getValue: (element: CentroCustoGridDTO) => element.unidadeNegocioCodigo,
    },
    {
      name: 'centroResultado',
      label: $localize`Centro Resultado`,
      getValue: (element: CentroCustoGridDTO) =>
        element.centroResultado ? $localize`Sim` : $localize`Não`,
    },
  ];

  tableActions: ActionModel<CentroCustoGridDTO>[] = [];

  toolbarActions: ToolbarActionModel[] = [];

  filtros: FilterProperty[] = [
    {
      property: 'nome',
      label: $localize`Nome`,
      filterType: FilterType.TEXT,
    },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service: CentroCustoService = inject(CentroCustoService);
  private auth: AuthService = inject(AuthService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo(
      'FINANCEIRO_CENTRO_CUSTO'
    );
    const canDelete = this.auth.hasAuthorityDeletarToModulo(
      'FINANCEIRO_CENTRO_CUSTO'
    );
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      'FINANCEIRO_CENTRO_CUSTO'
    );

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        action: (element: CentroCustoGridDTO) =>
          this.openDetail.emit(element.id),
      });
    }

    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        action: (element: CentroCustoGridDTO) => {
          this.service.delete(element.id).subscribe(() => this.listarCentros());
        },
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

    this.listarCentros();
  }

  listarCentros() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.rows = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listarCentros();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listarCentros();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarCentros();
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

  refreshList() {
    this.listarCentros();
  }
}
