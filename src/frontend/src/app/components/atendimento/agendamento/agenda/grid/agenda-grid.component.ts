import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { BaseComponent } from '../../../../base/base.component';
import { AgendaService } from '../agenda.service';
import { AgendaGridDTO } from '../model/agenda-grid-dto';
import { AuthService } from '../../../../base/auth/auth-service';
import { DialogService } from '../../../../base/dialog/dialog.service';
import { DialogResult } from '../../../../base/dialog/dialog.model';
import { TableComponent } from '../../../../base/table/table.component';
import { ColumnModel } from '../../../../base/table/column.model';
import { ActionModel } from '../../../../base/table/action.model';
import { ToolbarActionModel } from '../../../../base/model/toolbar-action.model';
import { PaginationEvent } from '../../../../base/pagination/pagination-event.model';
import { PaginationComponent } from '../../../../base/pagination/pagination.component';
import { FilterProperty, FilterComponent, FilterType } from '../../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../../base/model/filter-dto';
import { PageRequest, Order } from '../../../../base/model/page-request';
import { AuditInfoComponent, AuditInfoData } from '../../../../base/audit-info/audit-info.component';
import { Response } from '../../../../base/model/response';
import { SystemModuleKey } from '../../../../base/enum/system-module-key.enum';

@Component({
  selector: 'gi-agenda-grid',
  standalone: true,
  imports: [BaseComponent, TableComponent, PaginationComponent, FilterComponent, AuditInfoComponent],
  providers: [AgendaService],
  templateUrl: './agenda-grid.component.html',
  styleUrl: './agenda-grid.component.css',
})
export class AgendaGridComponent implements OnInit {
  titulo = $localize`Agendas`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  rows: AgendaGridDTO[] = [];

  columns: ColumnModel<AgendaGridDTO>[] = [
    { name: 'nome',             label: $localize`Nome`,          getValue: (e) => e.nome ?? '' },
    { name: 'profissionalNome', label: $localize`Profissional`,  getValue: (e) => e.profissionalNome ?? '' },
    { name: 'setorNome',        label: $localize`Setor`,         getValue: (e) => e.setorNome ?? '' },
    { name: 'ativo',            label: $localize`Ativo`,         getValue: (e) => e.ativo ? $localize`Sim` : $localize`Não` },
  ];

  tableActions: ActionModel<AgendaGridDTO>[] = [];
  toolbarActions: ToolbarActionModel[] = [];

  filterProperties: FilterProperty[] = [
    { property: 'nome', label: $localize`Nome`, filterType: FilterType.TEXT },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service = inject(AgendaService);
  private auth = inject(AuthService);
  private dialogService = inject(DialogService);

  constructor() {
    this.buildTableActions();
    this.buildToolbarActions();
  }

  private buildToolbarActions(): void {
    if (this.auth.hasAuthorityEditarToModulo(SystemModuleKey.AGENDAMENTO_AGENDA)) {
      this.toolbarActions.push({
        icon: 'add',
        title: $localize`Nova Agenda`,
        action: () => this.openDetail.emit('add'),
      });
    }
  }

  private buildTableActions(): void {
    if (this.auth.hasAuthorityVisualizarToModulo(SystemModuleKey.AGENDAMENTO_AGENDA)) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (e: AgendaGridDTO) => this.openDetail.emit(e.id),
      });
    }

    if (this.auth.hasAuthorityDeletarToModulo(SystemModuleKey.AGENDAMENTO_AGENDA)) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (e: AgendaGridDTO) => {
          this.dialogService
            .showYesNo($localize`Confirmar Exclusão`, $localize`Deseja realmente excluir esta agenda?`)
            .subscribe((result) => {
              if (result === DialogResult.YES) {
                if (e.id) this.service.delete(e.id).subscribe(() => this.listar());
              }
            });
        },
      });
    }

    if (this.auth.hasAuthorityAuditarToModulo(SystemModuleKey.AGENDAMENTO_AGENDA)) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: $localize`Auditoria`,
        action: (e: AgendaGridDTO) => this.loadAuditInfo(e.id),
      });
    }
  }

  ngOnInit(): void {
    this.listar();
  }

  listar(): void {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.rows = response.body.content ?? [];
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]): void {
    this.request.order = order;
    this.listar();
  }

  paginate(event: PaginationEvent): void {
    this.request.page = event.pageNumber;
    this.request.size = event.itemsPerPage;
    this.listar();
  }

  filter(filter: FilterDTO): void {
    this.request.filter = filter;
    this.listar();
  }

  closeFilter(): void {
    this.hideFilters = true;
  }

  loadAuditInfo(id: string | undefined): void {
    if (!id) return;
    this.service.getAuditInfo(id).subscribe((response: Response<AuditInfoData>) => {
      if (response.body) {
        this.auditInfoData = response.body;
        this.showAuditInfo = true;
      }
    });
  }

  closeAuditInfo(): void {
    this.showAuditInfo = false;
    this.auditInfoData = null;
  }
}
