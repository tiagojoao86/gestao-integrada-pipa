import { Component, EventEmitter, inject, Output } from '@angular/core';
import {
  RegisterActionToolbar,
  BaseComponent,
} from '../../../base/base.component';
import { PerfilService } from '../perfil.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { PerfilGridDTO } from '../model/perfil-grid-dto';
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
  FiltroComponent,
  FilterType,
} from '../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';

@Component({
  selector: 'gi-perfil-grid',
  standalone: true,
  imports: [
    BaseComponent,
    CommonModule,
    TableComponent,
    PaginatorComponent,
    FiltroComponent,
  ],
  providers: [PerfilService, DatePipe],
  templateUrl: './perfil-grid.component.html',
  styleUrl: './perfil-grid.component.css',
})
export class PerfilGridComponent {
  titulo: string = $localize`Cadastro de perfis`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;

  perfisList: PerfilGridDTO[] = [];

  colunas: DataSourceColumn[] = [
    {
      name: 'nome',
      label: $localize`Nome`,
      getValue: (element: PerfilGridDTO) => {
        return element.nome;
      },
    },
    {
      name: 'createdAt',
      label: $localize`Criado em`,
      getValue: (element: PerfilGridDTO) => {
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
      filterType: FilterType.TEXTO,
    },
    {
      property: 'createdAt',
      label: $localize`Criado em`,
      filterType: FilterType.DATA,
    },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service: PerfilService = inject(PerfilService);
  private datePipe: DatePipe = inject(DatePipe);
  private auth: AuthService = inject(AuthService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo('CADASTRO_PERFIL');
    const canDelete = this.auth.hasAuthorityDeletarToModulo('CADASTRO_PERFIL');

    if (canView) {
      this.acoesTabela.push({ icon: 'edit_note', action: (element: PerfilGridDTO) => this.openDetail.emit(element.id)});
    }
    if (canDelete) {
      this.acoesTabela.push({ icon: 'delete', action: (element: PerfilGridDTO) => { this.service.delete(element.id).subscribe(() => this.listarPerfis()); }});
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

    if (this.auth.hasAuthorityEditarToModulo('CADASTRO_PERFIL')) {
      this.acoesTela.push({action: () => { this.openDetail.emit('add'); }, icon: 'add', title: $localize`Adicionar` + ' (alt + a)', shortcut: 'alt.a'});
    }

    this.acoesTela.push({ action: () => { this.alternarMostrarFiltros(); }, icon: 'search', title: $localize`Pesquisar` + ' (alt + p)', value: '0', shortcut: 'alt.p'});

    this.listarPerfis();
  }

  listarPerfis() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.perfisList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  ordenar(order: Order[]) {
    this.request.order = order;
    this.listarPerfis();
  }

  paginar(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;

    this.listarPerfis();
  }

  filtrar(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarPerfis();
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
    this.listarPerfis();
  }
}
