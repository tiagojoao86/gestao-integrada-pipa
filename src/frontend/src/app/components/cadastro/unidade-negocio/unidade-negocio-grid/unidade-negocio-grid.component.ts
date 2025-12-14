import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { UnidadeNegocioService } from '../unidade-negocio.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { UnidadeNegocioGridDTO } from '../model/unidade-negocio-grid-dto';

import { AuthService } from '../../../base/auth/auth-service';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';

import { TableComponent } from '../../../base/table/table.component';
import { ColumnModel } from '../../../base/table/column.model';
import { ActionModel } from '../../../base/table/action.model';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { PaginationEvent } from '../../../base/pagination/pagination-event.model';
import {
  FilterProperty,
  FilterType,
  FilterComponent,
} from '../../../base/filter/filter.component';
import { PaginationComponent } from '../../../base/pagination/pagination.component';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'gi-unidade-negocio-grid',
  imports: [
    BaseComponent,
    PaginationComponent,
    TableComponent,
    FilterComponent,
  ],
  providers: [UnidadeNegocioService, DatePipe],
  templateUrl: './unidade-negocio-grid.component.html',
  styleUrl: './unidade-negocio-grid.component.css',
})
export class UnidadeNegocioGridComponent {
  titulo: string = $localize`Unidades de Negócio`;

  @Output() openDetailEvent = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;

  unidadesList: UnidadeNegocioGridDTO[] = [];

  columns: ColumnModel<UnidadeNegocioGridDTO>[] = [
    {
      name: 'codigo',
      label: $localize`Código`,
      getValue: (element: UnidadeNegocioGridDTO) => {
        return element.codigo;
      },
    },
    {
      name: 'nome',
      label: $localize`Nome`,
      getValue: (element: UnidadeNegocioGridDTO) => {
        return element.nome;
      },
    },
    {
      name: 'cnpj',
      label: $localize`CNPJ`,
      getValue: (element: UnidadeNegocioGridDTO) => {
        return element.cnpj || '-';
      },
    },
    {
      name: 'ativa',
      label: $localize`Ativa`,
      getValue: (element: UnidadeNegocioGridDTO) => {
        return element.ativa ? $localize`Sim` : $localize`Não`;
      },
    },
  ];

  tableActions: ActionModel<UnidadeNegocioGridDTO>[] = [];

  toolbarActions: ToolbarActionModel[] = [];

  filtros: FilterProperty[] = [
    {
      property: 'codigo',
      label: $localize`Código`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'nome',
      label: $localize`Nome`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'cnpj',
      label: $localize`CNPJ`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'ativa',
      label: $localize`Ativa`,
      filterType: FilterType.BOOLEAN,
      options: [
        { label: $localize`Sim`, key: 'true' },
        { label: $localize`Não`, key: 'false' },
      ],
    },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service: UnidadeNegocioService = inject(UnidadeNegocioService);
  private datePipe: DatePipe = inject(DatePipe);
  private auth: AuthService = inject(AuthService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo(
      'CADASTRO_UNIDADE_NEGOCIO'
    );
    const canDelete = this.auth.hasAuthorityDeletarToModulo(
      'CADASTRO_UNIDADE_NEGOCIO'
    );
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      'CADASTRO_UNIDADE_NEGOCIO'
    );

    if (canView) {
      this.tableActions.push({
        action: (element: UnidadeNegocioGridDTO) => {
          this.openDetail(element.id);
        },
        icon: 'edit_note',
      });
    }

    if (canEdit) {
      this.toolbarActions.push({
        action: () => {
          this.openDetail('add');
        },
        icon: 'add',
        title: $localize`Adicionar`,
      });
    }

    if (canDelete) {
      this.tableActions.push({
        action: (element: UnidadeNegocioGridDTO) => {
          this.excluir(element);
        },
        icon: 'delete',
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

    this.listarUnidades();
  }

  listarUnidades() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.unidadesList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listarUnidades();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listarUnidades();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarUnidades();
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

  openDetail(id: string) {
    this.openDetailEvent.emit(id);
  }

  excluir(element: UnidadeNegocioGridDTO) {
    this.service.delete(element.id).subscribe(() => {
      this.listarUnidades();
    });
  }

  refreshList() {
    this.listarUnidades();
  }
}
