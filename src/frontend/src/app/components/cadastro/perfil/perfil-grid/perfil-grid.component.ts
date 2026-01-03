import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { PerfilService } from '../perfil.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { PerfilGridDTO } from '../model/perfil-grid-dto';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../../base/auth/auth-service';
import { TableComponent } from '../../../base/table/table.component';
import { PaginationComponent } from '../../../base/pagination/pagination.component';
import {
  FilterProperty,
  FilterComponent,
  FilterType,
} from '../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';
import { PaginationEvent } from '../../../base/pagination/pagination-event.model';
import { ColumnModel } from '../../../base/table/column.model';
import { ActionModel } from '../../../base/table/action.model';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import {
  AuditInfoComponent,
  AuditInfoData,
} from '../../../base/audit-info/audit-info.component';
import { Response } from '../../../base/model/response';

@Component({
  selector: 'gi-perfil-grid',
  standalone: true,
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
    AuditInfoComponent,
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
  showDeleted = false;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  perfisList: PerfilGridDTO[] = [];

  columns: ColumnModel<PerfilGridDTO>[] = [
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
        return this.datePipe.transform(element.createdAt, 'shortDate');
      },
    },
  ];

  tableActions: ActionModel<PerfilGridDTO>[] = [];

  toolbarActions: ToolbarActionModel[] = [];

  filtros: FilterProperty[] = [
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

  private service: PerfilService = inject(PerfilService);
  private datePipe: DatePipe = inject(DatePipe);
  private auth: AuthService = inject(AuthService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo(
      SystemModuleKey.CADASTRO_PERFIL
    );
    const canDelete = this.auth.hasAuthorityDeletarToModulo(
      SystemModuleKey.CADASTRO_PERFIL
    );

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (element: PerfilGridDTO) => this.openDetail.emit(element.id),
      });
    }
    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (element: PerfilGridDTO) => {
          this.service.delete(element.id).subscribe(() => this.listPerfis());
        },
      });
    }

    const canAudit = this.auth.hasAuthorityAuditarToModulo(
      SystemModuleKey.CADASTRO_PERFIL
    );

    if (canAudit) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: 'Visualizar auditoria',
        action: (element: PerfilGridDTO) => this.loadAuditInfo(element.id),
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

    if (this.auth.hasAuthorityEditarToModulo(SystemModuleKey.CADASTRO_PERFIL)) {
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

    if (
      this.auth.hasAuthorityAuditarToModulo(SystemModuleKey.CADASTRO_PERFIL)
    ) {
      this.toolbarActions.unshift({
        action: () => {
          this.toggleShowDeleted();
        },
        icon: 'visibility',
        title: $localize`Mostrar excluídos` + ' (alt + d)',
        shortcut: 'alt.d',
      });
    }

    this.listPerfis();
  }

  listPerfis() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.perfisList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listPerfis();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;

    this.listPerfis();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.request.filter.showDeleted = this.showDeleted;
    this.listPerfis();
    this.updateFilterBadge(filter);
  }

  closeFilter() {
    this.toggleShowFilters();
  }

  updateFilterBadge(filter: FilterDTO) {
    const acao = this.toolbarActions.filter((it) => it.icon === 'search');
    if (acao.length > 0) {
      if (filter && filter.items) {
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
    this.listPerfis();
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
    this.listPerfis();
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
