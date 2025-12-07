import { Component, EventEmitter, inject, Output } from '@angular/core';
import {
  RegisterActionToolbar,
  BaseComponent,
} from '../../../base/base.component';
import { PessoaService } from '../pessoa.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { PessoaGridDTO } from '../model/pessoa-grid-dto';
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
import { TipoPessoa } from '../model/pessoa-dto';

@Component({
  selector: 'gi-pessoa-grid',
  imports: [
    BaseComponent,
    CommonModule,
    TableComponent,
    PaginatorComponent,
    FilterComponent,
  ],
  providers: [PessoaService, DatePipe],
  templateUrl: './pessoa-grid.component.html',
  styleUrl: './pessoa-grid.component.css',
})
export class PessoaGridComponent {
  titulo: string = $localize`Cadastro de pessoas`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;

  pessoasList: PessoaGridDTO[] = [];

  colunas: DataSourceColumn[] = [
    {
      name: 'nome',
      label: $localize`Nome`,
      getValue: (element: PessoaGridDTO) => {
        return element.nome;
      },
    },
    {
      name: 'documento',
      label: $localize`CPF/CNPJ`,
      getValue: (element: PessoaGridDTO) => {
        return element.documento;
      },
    },
    {
      name: 'tipoPessoa',
      label: $localize`Tipo`,
      getValue: (element: PessoaGridDTO) => {
        return element.tipoPessoa === 'FISICA'
          ? $localize`Pessoa Física`
          : $localize`Pessoa Jurídica`;
      },
    },
    {
      name: 'ativa',
      label: $localize`Ativa`,
      getValue: (element: PessoaGridDTO) => {
        return element.ativa ? $localize`Sim` : $localize`Não`;
      },
    },
    {
      name: 'createdAt',
      label: $localize`Criado em`,
      getValue: (element: PessoaGridDTO) => {
        return this.datePipe.transform(element.createdAt, 'dd/MM/yyyy');
      },
    },
  ];

  acoesTabela: Action[] = [];

  acoesTela: RegisterActionToolbar[] = [];
  filtros: FilterProperty[] = [
    {
      property: 'nome',
      label: $localize`Nome`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'cpf',
      label: $localize`CPF`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'cnpj',
      label: $localize`CNPJ`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'tipoPessoa',
      label: $localize`Tipo`,
      filterType: FilterType.SELECT,
      options: TipoPessoa.getList().map((tp) => ({
        label: tp.getLabel(),
        key: tp.getKey(),
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
    {
      property: 'createdAt',
      label: $localize`Criado em`,
      filterType: FilterType.DATE,
    },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service: PessoaService = inject(PessoaService);
  private datePipe: DatePipe = inject(DatePipe);
  private auth: AuthService = inject(AuthService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo('CADASTRO_PESSOA');
    const canDelete = this.auth.hasAuthorityDeletarToModulo('CADASTRO_PESSOA');

    if (canView) {
      this.acoesTabela.push({
        icon: 'edit_note',
        action: (element: PessoaGridDTO) => this.openDetail.emit(element.id),
      });
    }
    if (canDelete) {
      this.acoesTabela.push({
        icon: 'delete',
        action: (element: PessoaGridDTO) => {
          this.service.delete(element.id).subscribe(() => this.listarPessoas());
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

    if (this.auth.hasAuthorityEditarToModulo('CADASTRO_PESSOA')) {
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

    this.listarPessoas();
  }

  listarPessoas() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.pessoasList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  ordenar(order: Order[]) {
    this.request.order = order;
    this.listarPessoas();
  }

  paginar(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;

    this.listarPessoas();
  }

  filtrar(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarPessoas();
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
    this.listarPessoas();
  }
}
