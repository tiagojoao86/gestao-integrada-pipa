import { Component, EventEmitter, inject, OnInit, Output } from '@angular/core';
import { BaseComponent } from '../../../../base/base.component';
import { AgendamentoService } from '../agendamento.service';
import { AgendamentoGridDTO } from '../model/agendamento-grid-dto';
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
import { MessageService } from '../../../../base/messages/messages.service';

const STATUS_LABELS: Record<string, string> = {
  AGENDADO: $localize`Agendado`,
  CANCELADO: $localize`Cancelado`,
  REALIZADO: $localize`Realizado`,
};

@Component({
  selector: 'gi-agendamento-grid',
  standalone: true,
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
    AuditInfoComponent,
  ],
  providers: [AgendamentoService],
  templateUrl: './agendamento-grid.component.html',
  styleUrl: './agendamento-grid.component.css',
})
export class AgendamentoGridComponent implements OnInit {
  titulo = $localize`Agendamentos`;

  @Output() openDetail = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  rows: AgendamentoGridDTO[] = [];

  columns: ColumnModel<AgendamentoGridDTO>[] = [
    { name: 'agendaNome',       label: $localize`Agenda`,       getValue: (e) => e.agendaNome ?? '' },
    { name: 'pacienteNome',     label: $localize`Paciente`,     getValue: (e) => e.pacienteNome ?? '' },
    { name: 'primeiraData',     label: $localize`Data`,         getValue: (e) => this.formatDate(e.primeiraData) },
    { name: 'qtdHorarios',      label: $localize`Horários`,     getValue: (e) => String(e.qtdHorarios ?? 0) },
    { name: 'convenioNome',     label: $localize`Convênio`,     getValue: (e) => e.convenioNome ?? '' },
    { name: 'procedimentoNome', label: $localize`Procedimento`, getValue: (e) => e.procedimentoNome ?? '' },
    { name: 'status',           label: $localize`Status`,
      getValue: (e) => STATUS_LABELS[e.status ?? ''] ?? (e.status ?? '') },
  ];

  tableActions: ActionModel<AgendamentoGridDTO>[] = [];
  toolbarActions: ToolbarActionModel[] = [];

  filterProperties: FilterProperty[] = [
    { property: 'pacienteNome', label: $localize`Paciente`, filterType: FilterType.TEXT },
    { property: 'status',       label: $localize`Status`,   filterType: FilterType.TEXT },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service = inject(AgendamentoService);
  private auth = inject(AuthService);
  private dialogService = inject(DialogService);
  private messages = inject(MessageService);

  constructor() {
    this.buildTableActions();
    this.buildToolbarActions();
  }

  private buildToolbarActions(): void {
    if (this.auth.hasAuthorityEditarToModulo(SystemModuleKey.AGENDAMENTO_AGENDAMENTO)) {
      this.toolbarActions.push({
        icon: 'add',
        title: $localize`Novo Agendamento`,
        action: () => this.openDetail.emit('add'),
      });
    }
  }

  private buildTableActions(): void {
    if (this.auth.hasAuthorityVisualizarToModulo(SystemModuleKey.AGENDAMENTO_AGENDAMENTO)) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (e: AgendamentoGridDTO) => this.openDetail.emit(e.id),
      });
    }

    if (this.auth.hasAuthorityEditarToModulo(SystemModuleKey.AGENDAMENTO_AGENDAMENTO)) {
      this.tableActions.push({
        icon: 'check_circle',
        title: $localize`Realizar`,
        action: (e: AgendamentoGridDTO) => {
          if (e.id) this.realizar(e.id);
        },
      });

      this.tableActions.push({
        icon: 'cancel',
        title: $localize`Cancelar`,
        action: (e: AgendamentoGridDTO) => {
          if (e.id) this.cancelar(e.id);
        },
      });
    }

    if (this.auth.hasAuthorityDeletarToModulo(SystemModuleKey.AGENDAMENTO_AGENDAMENTO)) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (e: AgendamentoGridDTO) => {
          this.dialogService
            .showYesNo(
              $localize`Confirmar Exclusão`,
              $localize`Deseja realmente excluir este agendamento?`
            )
            .subscribe((result) => {
              if (result === DialogResult.YES) {
                if (e.id) this.service.delete(e.id).subscribe(() => this.listar());
              }
            });
        },
      });
    }

    if (this.auth.hasAuthorityAuditarToModulo(SystemModuleKey.AGENDAMENTO_AGENDAMENTO)) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: $localize`Auditoria`,
        action: (e: AgendamentoGridDTO) => this.loadAuditInfo(e.id),
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

  private realizar(id: string): void {
    this.dialogService
      .showYesNo($localize`Confirmar Realização`, $localize`Marcar este agendamento como realizado?`)
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.realizar(id).subscribe(() => {
            this.messages.sucesso($localize`Agendamento marcado como realizado.`);
            this.listar();
          });
        }
      });
  }

  private cancelar(id: string): void {
    this.dialogService
      .showYesNo($localize`Confirmar Cancelamento`, $localize`Deseja cancelar este agendamento?`)
      .subscribe((result) => {
        if (result === DialogResult.YES) {
          this.service.cancelar(id).subscribe(() => {
            this.messages.sucesso($localize`Agendamento cancelado com sucesso.`);
            this.listar();
          });
        }
      });
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

  private formatDate(value: string | undefined): string {
    if (!value) return '';
    try {
      return new Date(value + 'T00:00:00').toLocaleDateString('pt-BR');
    } catch {
      return value;
    }
  }
}
