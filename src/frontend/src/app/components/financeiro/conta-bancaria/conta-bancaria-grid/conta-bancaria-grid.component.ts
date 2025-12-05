import { Component, EventEmitter, inject, Output } from '@angular/core';
import {
  RegisterActionToolbar,
  BaseComponent,
} from '../../../base/base.component';
import { ContaBancariaService } from '../conta-bancaria.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { ContaBancariaGridDTO } from '../model/conta-bancaria-grid-dto';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../../base/auth/auth-service';
import {
  Action,
  DataSourceColumn,
  TableComponent,
} from '../../../base/table/table.component';
import {
  PaginationEvent,
  PaginatorComponent,
} from '../../../base/paginator/paginator.component';
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
    CommonModule,
    TableComponent,
    PaginatorComponent,
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

  colunas: DataSourceColumn[] = [
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
      name: 'ativa',
      label: $localize`Ativa`,
      getValue: (element: ContaBancariaGridDTO) => {
        return element.ativa ? $localize`Sim` : $localize`Não`;
      },
    },
  ];

  acoesTabela: Action[] = [];

  acoesTela: RegisterActionToolbar[] = [];

  filtros: FilterProperty[] = [
    {
      property: 'nome',
      label: $localize`Nome`,
      filterType: FilterType.TEXTO,
    },
    {
      property: 'banco',
      label: $localize`Banco`,
      filterType: FilterType.TEXTO,
    },
    {
      property: 'tipo',
      label: $localize`Tipo`,
      filterType: FilterType.SELECAO,
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
      this.acoesTabela.push({
        icon: 'edit_note',
        action: (element: ContaBancariaGridDTO) =>
          this.openDetail.emit(element.id),
      });
    }

    if (canDelete) {
      this.acoesTabela.push({
        icon: 'delete',
        action: (element: ContaBancariaGridDTO) => {
          this.service.delete(element.id).subscribe(() => this.listarContas());
        },
      });
    }

    this.acoesTela = [
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
      this.acoesTela.push({
        action: () => {
          this.openDetail.emit('add');
        },
        icon: 'add',
        title: $localize`Adicionar` + ' (alt + a)',
        shortcut: 'alt.a',
      });
    }

    this.acoesTela.push({
      action: () => {
        this.alternarMostrarFiltros();
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

  ordenar(order: Order[]) {
    this.request.order = order;
    this.listarContas();
  }

  paginar(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listarContas();
  }

  filtrar(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarContas();
    this.ajustaBadgePesquisa(filter);
  }

  cancelar() {
    this.alternarMostrarFiltros();
  }

  ajustaBadgePesquisa(filter: FilterDTO) {
    const acao = this.acoesTela.filter((it) => it.icon === 'search');
    if (acao.length > 0) {
      if (filter) {
        acao[0].value = filter.items.length + '';
        return;
      }
      acao[0].value = '0';
    }
  }

  alternarMostrarFiltros() {
    this.hideFilters = !this.hideFilters;
  }

  refreshList() {
    this.listarContas();
  }
}
