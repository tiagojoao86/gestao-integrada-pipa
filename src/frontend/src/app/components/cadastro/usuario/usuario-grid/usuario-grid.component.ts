import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { UsuarioService } from '../usuario.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { UsuarioGridDTO } from '../model/usuario-grid-dto';
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

@Component({
  selector: 'gi-usuario-grid',
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
  ],
  providers: [UsuarioService, DatePipe],
  templateUrl: './usuario-grid.component.html',
  styleUrl: './usuario-grid.component.css',
})
export class UsuarioGridComponent {
  titulo: string = $localize`Cadastro de usuários`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;

  usuariosList: UsuarioGridDTO[] = [];

  columns: ColumnModel<UsuarioGridDTO>[] = [
    {
      name: 'nome',
      label: $localize`Nome`,
      getValue: (element: UsuarioGridDTO) => {
        return element.nome;
      },
    },
    {
      name: 'login',
      label: $localize`Login`,
      getValue: (element: UsuarioGridDTO) => {
        return element.login;
      },
    },
    {
      name: 'createdAt',
      label: $localize`Criado em`,
      getValue: (element: UsuarioGridDTO) => {
        return this.datePipe.transform(element.createdAt, 'shortDate');
      },
    },
  ];

  tableActions: ActionModel<UsuarioGridDTO>[] = [];

  toolbarActions: ToolbarActionModel[] = [];
  filtros: FilterProperty[] = [
    {
      property: 'login',
      label: $localize`Login`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'nome',
      label: $localize`Nome`,
      filterType: FilterType.TEXT,
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

  private service: UsuarioService = inject(UsuarioService);
  private datePipe: DatePipe = inject(DatePipe);
  private auth: AuthService = inject(AuthService);

  constructor() {
    const canView =
      this.auth.hasAuthorityVisualizarToModulo('CADASTRO_USUARIO');
    const canDelete = this.auth.hasAuthorityDeletarToModulo('CADASTRO_USUARIO');

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        action: (element: UsuarioGridDTO) => this.openDetail.emit(element.id),
      });
    }
    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        action: (element: UsuarioGridDTO) => {
          this.service
            .delete(element.id)
            .subscribe(() => this.listarUsuarios());
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

    if (this.auth.hasAuthorityEditarToModulo('CADASTRO_USUARIO')) {
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

    this.listarUsuarios();
  }

  listarUsuarios() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.usuariosList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listarUsuarios();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;

    this.listarUsuarios();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarUsuarios();
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
    this.listarUsuarios();
  }
}
