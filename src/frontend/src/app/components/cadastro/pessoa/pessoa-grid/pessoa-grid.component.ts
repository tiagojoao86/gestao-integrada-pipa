import { Component, EventEmitter, inject, Output } from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { PessoaService } from '../pessoa.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { PessoaGridDTO } from '../model/pessoa-grid-dto';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../../base/auth/auth-service';
import { DialogService } from '../../../base/dialog/dialog.service';
import { DialogResult } from '../../../base/dialog/dialog.model';
import { TableComponent } from '../../../base/table/table.component';
import { PaginationComponent } from '../../../base/pagination/pagination.component';
import {
  FilterProperty,
  FilterComponent,
  FilterType,
} from '../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';
import { TipoPessoa } from '../model/pessoa-dto';
import { PaginationEvent } from '../../../base/pagination/pagination-event.model';
import { ColumnModel } from '../../../base/table/column.model';
import { ActionModel } from '../../../base/table/action.model';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import {
  AuditInfoComponent,
  AuditInfoData,
} from '../../../base/audit-info/audit-info.component';
import { Response } from '../../../base/model/response';

@Component({
  selector: 'gi-pessoa-grid',
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
    AuditInfoComponent,
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
  showDeleted = false;
  showAuditInfo = false;
  auditInfoData: AuditInfoData | null = null;

  pessoasList: PessoaGridDTO[] = [];

  columns: ColumnModel<PessoaGridDTO>[] = [
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
        return this.datePipe.transform(element.createdAt, 'shortDate');
      },
    },
  ];

  tableActions: ActionModel<PessoaGridDTO>[] = [];

  toolbarActions: ToolbarActionModel[] = [];
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
      property: 'dataNascimento',
      label: $localize`Data de Nascimento`,
      filterType: FilterType.DATE,
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
  private dialogService: DialogService = inject(DialogService);

  constructor() {
    const canView = this.auth.hasAuthorityVisualizarToModulo(
      SystemModuleKey.CADASTRO_PESSOA
    );
    const canDelete = this.auth.hasAuthorityDeletarToModulo(
      SystemModuleKey.CADASTRO_PESSOA
    );

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        title: $localize`Editar`,
        action: (element: PessoaGridDTO) => this.openDetail.emit(element.id),
      });
    }
    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        title: $localize`Excluir`,
        action: (element: PessoaGridDTO) => {
          this.dialogService
            .showYesNo(
              $localize`Confirmar Exclusão`,
              $localize`Deseja realmente excluir o registro selecionado?`
            )
            .subscribe((result) => {
              if (result === DialogResult.YES) {
                this.service.delete(element.id).subscribe(() => this.listPessoas());
              }
            });
        },
      });
    }

    const canAudit = this.auth.hasAuthorityAuditarToModulo(
      SystemModuleKey.CADASTRO_PESSOA
    );

    if (canAudit) {
      this.tableActions.push({
        icon: 'eye_tracking',
        iconType: 'material-symbols-outlined',
        title: 'Visualizar auditoria',
        action: (element: PessoaGridDTO) => this.loadAuditInfo(element.id),
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

    if (this.auth.hasAuthorityEditarToModulo(SystemModuleKey.CADASTRO_PESSOA)) {
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
      this.auth.hasAuthorityAuditarToModulo(SystemModuleKey.CADASTRO_PESSOA)
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

    this.listPessoas();
  }

  listPessoas() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.pessoasList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listPessoas();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;

    this.listPessoas();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.request.filter.showDeleted = this.showDeleted;
    this.listPessoas();
    this.updateFilterBadge(filter);
  }

  closeFilter() {
    this.toggleShowFilters();
  }

  updateFilterBadge(filter: FilterDTO) {
    const action = this.toolbarActions.filter((it) => it.icon === 'search');
    if (action.length > 0) {
      if (filter && filter.items) {
        action[0].value = filter.items.length + '';
        return;
      }
      action[0].value = '0';
    }
  }

  toggleShowFilters() {
    this.hideFilters = !this.hideFilters;
  }

  toggleShowDeleted() {
    this.showDeleted = !this.showDeleted;
    this.request.filter.showDeleted = this.showDeleted;
    this.updateShowDeletedIcon();
    this.listPessoas();
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
    this.listPessoas();
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
