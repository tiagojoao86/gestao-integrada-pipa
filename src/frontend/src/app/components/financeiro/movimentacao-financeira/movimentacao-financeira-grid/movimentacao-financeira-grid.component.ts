import {
  Component,
  EventEmitter,
  inject,
  Input,
  LOCALE_ID,
  Output,
} from '@angular/core';
import { MovimentacaoFinanceiraGridDTO } from '../model/movimentacao-financeira-grid.dto';
import { MovimentacaoFinanceiraService } from '../movimentacao-financeira.service';
import { BaseComponent } from '../../../base/base.component';
import { TableComponent } from '../../../base/table/table.component';
import { PaginationComponent } from '../../../base/pagination/pagination.component';
import { FilterComponent } from '../../../base/filter/filter.component';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { AuthService } from '../../../base/auth/auth-service';
import {
  FilterProperty,
  FilterType,
} from '../../../base/filter/filter.component';
import { FilterDTO, FilterLogicOperator } from '../../../base/model/filter-dto';
import { PageRequest, Order } from '../../../base/model/page-request';
import { LocaleUtils } from '../../../base/utils/locale-utils';
import { PaginationEvent } from '../../../base/pagination/pagination-event.model';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { ActionModel } from '../../../base/table/action.model';
import { ColumnModel } from '../../../base/table/column.model';
import { FormsModule } from "@angular/forms";

@Component({
  selector: 'gi-movimentacao-financeira-grid',
  templateUrl: './movimentacao-financeira-grid.component.html',
  styleUrls: ['./movimentacao-financeira-grid.component.css'],
  imports: [
    BaseComponent,
    TableComponent,
    PaginationComponent,
    FilterComponent,
    FormsModule
],
  providers: [MovimentacaoFinanceiraService, DatePipe, CurrencyPipe],
})
export class MovimentacaoFinanceiraGridComponent {
  titulo: string = $localize`Movimentações Financeiras`;
  @Input() movimentacoes: MovimentacaoFinanceiraGridDTO[] = [];
  @Output() selecionar = new EventEmitter<string>();
  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;

  filtros: FilterProperty[] = [];
  hideFilters = true;
  totalElements = 0;
  toolbarActions: ToolbarActionModel[] = [];
  tableActions: ActionModel<MovimentacaoFinanceiraGridDTO>[] = [];

  columns: ColumnModel<MovimentacaoFinanceiraGridDTO>[] = [
    {
      name: 'data',
      label: $localize`Data`,
      getValue: (element: MovimentacaoFinanceiraGridDTO) =>
        this.datePipe.transform(element.data, 'shortDate'),
    },
    {
      name: 'valor',
      label: $localize`Valor`,
      getValue: (element: MovimentacaoFinanceiraGridDTO) => {
        const currency = LocaleUtils.getCurrencyForLocale(this.locale);
        return this.currencyPipe.transform(element.valor, currency, 'symbol');
      },
    },
    {
      name: 'tipo',
      label: $localize`Tipo`,
      getValue: (element: MovimentacaoFinanceiraGridDTO) => {
        const map: Record<string, string> = {
          PAGAMENTO: $localize`Pagamento`,
          RECEBIMENTO: $localize`Recebimento`,
          ESTORNO: $localize`Estorno`,
          TRANSFERENCIA: $localize`Transferência`,
        };
        return map[element.tipo] || element.tipo;
      },
    },
    {
      name: 'unidadeNegocio',
      label: $localize`Unidade`,
      getValue: (element: MovimentacaoFinanceiraGridDTO) =>
        element.unidadeNegocioNome || '-',
    },
    {
      name: 'contaBancaria',
      label: $localize`Conta / Banco`,
      getValue: (element: MovimentacaoFinanceiraGridDTO) =>
        element.contaBancaria || element.contaBancariaNome || '-',
    },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service: MovimentacaoFinanceiraService = inject(
    MovimentacaoFinanceiraService
  );
  private datePipe: DatePipe = inject(DatePipe);
  private currencyPipe: CurrencyPipe = inject(CurrencyPipe);
  private auth: AuthService = inject(AuthService);
  private locale: string = inject(LOCALE_ID);

  constructor() {
    // configurar ações da tabela/toolbar
    const canView = this.auth.hasAuthorityVisualizarToModulo(
      'FINANCEIRO_MOVIMENTACAO'
    );
    const canDelete = this.auth.hasAuthorityDeletarToModulo(
      'FINANCEIRO_MOVIMENTACAO'
    );

    if (canView) {
      this.tableActions.push({
        icon: 'edit_note',
        action: (element: MovimentacaoFinanceiraGridDTO) =>
          this.selecionar.emit(element.id),
      });
    }
    if (canDelete) {
      this.tableActions.push({
        icon: 'delete',
        action: (element: MovimentacaoFinanceiraGridDTO) =>
          this.service
            .delete(element.id)
            .subscribe(() => this.listarMovimentacoes()),
      });
    }

    this.toolbarActions = [
      {
        action: () => this.refreshList(),
        icon: 'refresh',
        title: $localize`Atualizar`,
        shortcut: 'alt.r',
      },
    ];

    if (this.auth.hasAuthorityEditarToModulo('FINANCEIRO_MOVIMENTACAO')) {
      this.toolbarActions.push({
        action: () => this.selecionar.emit('add'),
        icon: 'add',
        title: $localize`Adicionar`,
        shortcut: 'alt.a',
      });
    }

    this.toolbarActions.push({
      action: () => this.toggleShowFilters(),
      icon: 'search',
      title: $localize`Pesquisar`,
      value: '0',
      shortcut: 'alt.p',
    });

    // definir filtros básicos
    this.filtros = [
      {
        property: 'tipo',
        label: $localize`Tipo`,
        filterType: FilterType.SELECT,
        options: [
          { key: 'PAGAMENTO', label: $localize`Pagamento` },
          { key: 'RECEBIMENTO', label: $localize`Recebimento` },
        ],
      },
      { property: 'data', label: $localize`Data`, filterType: FilterType.DATE },
    ];

    this.listarMovimentacoes();
  }

  listarMovimentacoes() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.movimentacoes = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(order: Order[]) {
    this.request.order = order;
    this.listarMovimentacoes();
  }

  paginate(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;
    this.listarMovimentacoes();
  }

  filter(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarMovimentacoes();
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
    this.listarMovimentacoes();
  }

  selecionarMovimentacao(id: string) {
    this.selecionar.emit(id);
  }
}
