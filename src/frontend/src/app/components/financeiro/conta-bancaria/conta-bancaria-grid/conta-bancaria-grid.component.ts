import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { ContaBancariaService } from '../conta-bancaria.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { ContaBancariaGridDTO } from '../model/conta-bancaria-grid-dto';
import { DatePipe } from '@angular/common';
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
import { TipoConta } from '../model/tipo-conta.enum';

@Component({
  selector: 'gi-conta-bancaria-grid',
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
  ],
  providers: [ContaBancariaService, DatePipe],
  templateUrl: './conta-bancaria-grid.component.html',
  styleUrl: './conta-bancaria-grid.component.css',
})
export class ContaBancariaGridComponent {
  titulo: string = $localize`Contas Bancárias`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;

  contasList: ContaBancariaGridDTO[] = [];

  columns: ColumnModel<ContaBancariaGridDTO>[] = [
    {
      name: 'nome',
      label: $localize`Nome`,
      getValue: (element: ContaBancariaGridDTO) => {
        return element.nome;
      },
    },
    {
      name: 'banco',
      label: $localize`Banco`,
      getValue: (element: ContaBancariaGridDTO) => {
        return element.banco || '-';
      },
    },
    {
      name: 'tipo',
      label: $localize`Tipo`,
      getValue: (element: ContaBancariaGridDTO) => {
        const tipoConta = TipoConta.getByKey(element.tipo);
        return tipoConta ? tipoConta.getLabel() : element.tipo;
      },
    },
    {
      name: 'saldoInicial',
      label: $localize`Saldo Inicial`,
      getValue: (element: ContaBancariaGridDTO) => {
        return element.saldoInicial != null
          ? `R$ ${element.saldoInicial.toFixed(2)}`
          : 'R$ 0,00';
      },
    },
    {
      name: 'unidadeNegocioCodigo',
      label: $localize`Unidade de Negócio`,
      getValue: (element: ContaBancariaGridDTO) => {
        return element.unidadeNegocioCodigo || '-';
      },
    },
    {
      name: 'ativa',
      label: $localize`Ativa`,
      getValue: (element: ContaBancariaGridDTO) => {
        return element.ativa ? $localize`Sim` : $localize`Não`;
      },
    },
  ];

  tableActions: ActionModel<ContaBancariaGridDTO>[] = [];

  toolbarActions: ToolbarActionModel[] = [];

  filtros: FilterProperty[] = [
    {
      property: 'nome',
      label: $localize`Nome`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'banco',
      label: $localize`Banco`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'tipo',
      label: $localize`Tipo`,
      filterType: FilterType.SELECT,
      options: TipoConta.getList().map((tipo) => ({
        label: tipo.getLabel(),
        key: tipo.getKey(),
      })),
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

  private service: ContaBancariaService = inject(ContaBancariaService);
  private datePipe: DatePipe = inject(DatePipe);
  private auth: AuthService = inject(AuthService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo(
      'CADASTRO_CONTA_BANCARIA'
    );
    const canDelete = this.auth.hasAuthorityDeletarToModulo(
      'CADASTRO_CONTA_BANCARIA'
    );
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      'CADASTRO_CONTA_BANCARIA'
    );

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        action: (element: ContaBancariaGridDTO) =>
          this.openDetail.emit(element.id),
      });
    }

    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        action: (element: ContaBancariaGridDTO) => {
          this.service.delete(element.id).subscribe(() => this.listarContas());
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

    this.listarContas();
  }

  listarContas() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.contasList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listarContas();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listarContas();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarContas();
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
    this.listarContas();
  }
}
