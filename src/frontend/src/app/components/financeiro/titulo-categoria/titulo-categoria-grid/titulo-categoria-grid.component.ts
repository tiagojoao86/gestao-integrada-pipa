import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { TituloCategoriaService } from '../titulo-categoria.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { TituloCategoriaGridDTO } from '../model/titulo-categoria-grid.dto';
import { AuthService } from '../../../base/auth/auth-service';
import { DialogService } from '../../../base/dialog/dialog.service';
import { DialogResult } from '../../../base/dialog/dialog.model';
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
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import {
  AuditInfoComponent,
  AuditInfoData,
} from '../../../base/audit-info/audit-info.component';
import { Response } from '../../../base/model/response';

@Component({
  selector: 'gi-titulo-categoria-grid',
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
    AuditInfoComponent,
  ],
  providers: [TituloCategoriaService],
  templateUrl: './titulo-categoria-grid.component.html',
  styleUrls: ['./titulo-categoria-grid.component.css'],
})
export class TituloCategoriaGridComponent {
  titulo: string = $localize`Categorias de Títulos`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;
  showDeleted = false;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  lista: TituloCategoriaGridDTO[] = [];

  columns: ColumnModel<TituloCategoriaGridDTO>[] = [
    {
      name: 'codigo',
      label: $localize`Código`,
      getValue: (element: TituloCategoriaGridDTO) => element.codigo || '-',
    },
    {
      name: 'nome',
      label: $localize`Nome`,
      getValue: (element: TituloCategoriaGridDTO) => element.nome || '-',
    },
    {
      name: 'tipo',
      label: $localize`Tipo`,
      getValue: (element: TituloCategoriaGridDTO) => element.tipo.label || '-',
    },
    {
      name: 'agrupadorNome',
      label: $localize`Agrupador`,
      getValue: (element: TituloCategoriaGridDTO) =>
        element.agrupadorNome || '-',
    },
    {
      name: 'descricao',
      label: $localize`Descrição`,
      getValue: (element: TituloCategoriaGridDTO) => element.descricao || '-',
    },
  ];

  tableActions: ActionModel<TituloCategoriaGridDTO>[] = [];

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
      property: 'descricao',
      label: $localize`Descrição`,
      filterType: FilterType.TEXT,
    },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service: TituloCategoriaService = inject(TituloCategoriaService);
  private auth: AuthService = inject(AuthService);
  private dialogService: DialogService = inject(DialogService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo(
      SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA
    );
    const canDelete = this.auth.hasAuthorityDeletarToModulo(
      SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA
    );
    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA
    );

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (element: TituloCategoriaGridDTO) =>
          this.openDetail.emit(element.id!),
      });
    }

    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (element: TituloCategoriaGridDTO) => {
          this.dialogService
            .showYesNo(
              $localize`Confirmar Exclusão`,
              $localize`Deseja realmente excluir o registro selecionado?`
            )
            .subscribe((result) => {
              if (result === DialogResult.YES) {
                this.service.delete(element.id!).subscribe(() => this.listar());
              }
            });
        },
      });
    }

    const canAudit = this.auth.hasAuthorityAuditarToModulo(
      SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA
    );

    if (canAudit) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: 'Visualizar auditoria',
        action: (element: TituloCategoriaGridDTO) => this.loadAuditInfo(element.id!),
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

    if (
      this.auth.hasAuthorityAuditarToModulo(
        SystemModuleKey.FINANCEIRO_TITULO_CATEGORIA
      )
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

    this.listar();
  }

  listar() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.lista = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listar();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listar();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.request.filter.showDeleted = this.showDeleted;
    this.listar();
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
    this.listar();
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
    this.listar();
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
