import {
  Component,
  EventEmitter,
  inject,
  LOCALE_ID,
  Output,
} from '@angular/core';
import {
  RegisterActionToolbar,
  BaseComponent,
} from '../../../base/base.component';
import { TituloService } from '../titulo.service';
import { Order, PageRequest } from '../../../base/model/page-request';
import { TituloGridDTO } from '../model/titulo-grid-dto';
import { DatePipe, CurrencyPipe } from '@angular/common';
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

@Component({
  selector: 'gi-titulo-grid',
  imports: [BaseComponent, TableComponent, PaginatorComponent, FilterComponent],
  providers: [TituloService, DatePipe, CurrencyPipe],
  templateUrl: './titulo-grid.component.html',
  styleUrl: './titulo-grid.component.css',
})
export class TituloGridComponent {
  titulo: string = $localize`Cadastro de títulos`;

  @Output() openDetailEvent = new EventEmitter<string>();

  itensPorPagina = PaginationEvent.DEFAULT_PAGE_SIZE;
  totalElements = 0;
  hideFilters = true;

  titulosList: TituloGridDTO[] = [];

  colunas: DataSourceColumn[] = [
    {
      name: 'tipo',
      label: $localize`Tipo`,
      getValue: (element: TituloGridDTO) => {
        return element.tipo === 'A_PAGAR'
          ? $localize`A Pagar`
          : $localize`A Receber`;
      },
    },
    {
      name: 'status',
      label: $localize`Status`,
      getValue: (element: TituloGridDTO) => {
        return this.getStatusLabel(element.status);
      },
    },
    {
      name: 'numeroDocumento',
      label: $localize`Nº Doc.`,
      getValue: (element: TituloGridDTO) => {
        return element.numeroDocumento || '-';
      },
    },
    {
      name: 'descricao',
      label: $localize`Descrição`,
      getValue: (element: TituloGridDTO) => {
        return element.descricao;
      },
    },
    {
      name: 'pessoaNome',
      label: $localize`Pessoa`,
      getValue: (element: TituloGridDTO) => {
        return element.pessoaNome;
      },
    },
    {
      name: 'unidadeNegocioCodigo',
      label: $localize`Unidade`,
      getValue: (element: TituloGridDTO) => {
        return element.unidadeNegocioCodigo || '-';
      },
    },
    {
      name: 'valorOriginal',
      label: $localize`Valor`,
      getValue: (element: TituloGridDTO) => {
        const currency = this.getCurrencyForLocale();
        return this.currencyPipe.transform(
          element.valorOriginal,
          currency,
          'symbol'
        );
      },
    },
    {
      name: 'saldo',
      label: $localize`Saldo`,
      getValue: (element: TituloGridDTO) => {
        const currency = this.getCurrencyForLocale();
        return this.currencyPipe.transform(element.saldo, currency, 'symbol');
      },
    },
    {
      name: 'dataVencimento',
      label: $localize`Vencimento`,
      getValue: (element: TituloGridDTO) => {
        return this.datePipe.transform(element.dataVencimento, 'shortDate');
      },
    },
    {
      name: 'parcelamento',
      label: $localize`Parcela`,
      getValue: (element: TituloGridDTO) => {
        return element.parcelamento || '-';
      },
    },
  ];

  acoesTabela: Action[] = [];

  acoesTela: RegisterActionToolbar[] = [];
  filtros: FilterProperty[] = [
    {
      property: 'tipo',
      label: $localize`Tipo`,
      filterType: FilterType.SELECT,
      options: [
        { key: 'A_PAGAR', label: $localize`A Pagar` },
        { key: 'A_RECEBER', label: $localize`A Receber` },
      ],
    },
    {
      property: 'status',
      label: $localize`Status`,
      filterType: FilterType.SELECT,
      options: [
        { key: 'ABERTO', label: $localize`Aberto` },
        { key: 'PARCIAL', label: $localize`Parcial` },
        { key: 'PAGO', label: $localize`Pago` },
        { key: 'CANCELADO', label: $localize`Cancelado` },
        { key: 'VENCIDO', label: $localize`Vencido` },
      ],
    },
    {
      property: 'numeroDocumento',
      label: $localize`Nº Documento`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'descricao',
      label: $localize`Descrição`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'pessoaNome',
      label: $localize`Pessoa`,
      filterType: FilterType.TEXT,
    },
    {
      property: 'dataVencimento',
      label: $localize`Data Vencimento`,
      filterType: FilterType.DATE,
    },
  ];

  request = new PageRequest(
    { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
    this.itensPorPagina,
    0,
    []
  );

  private service: TituloService = inject(TituloService);
  private datePipe: DatePipe = inject(DatePipe);
  private currencyPipe: CurrencyPipe = inject(CurrencyPipe);
  private auth: AuthService = inject(AuthService);
  private locale: string = inject(LOCALE_ID);

  constructor() {
    const canView =
      this.auth.hasAuthorityVisualizarToModulo('FINANCEIRO_TITULO');
    const canDelete =
      this.auth.hasAuthorityDeletarToModulo('FINANCEIRO_TITULO');

    if (canView) {
      this.acoesTabela.push({
        icon: 'edit_note',
        action: (element: TituloGridDTO) =>
          this.openDetailEvent.emit(element.id),
      });
    }
    if (canDelete) {
      this.acoesTabela.push({
        icon: 'delete',
        action: (element: TituloGridDTO) => {
          this.service.delete(element.id).subscribe(() => this.listarTitulos());
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

    if (this.auth.hasAuthorityEditarToModulo('FINANCEIRO_TITULO')) {
      this.acoesTela.push({
        action: () => {
          this.openDetailEvent.emit('add');
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

    this.listarTitulos();
  }

  getStatusLabel(status: string): string {
    const statusMap: Record<string, string> = {
      ABERTO: $localize`Aberto`,
      PARCIAL: $localize`Parcial`,
      PAGO: $localize`Pago`,
      CANCELADO: $localize`Cancelado`,
      VENCIDO: $localize`Vencido`,
    };
    return statusMap[status] || status;
  }

  listarTitulos() {
    this.service.list(this.request).subscribe((response) => {
      if (response.body) {
        this.titulosList = response.body.content;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  ordenar(order: Order[]) {
    this.request.order = order;
    this.listarTitulos();
  }

  paginar(page: PaginationEvent) {
    this.request.page = page.pageNumber;
    this.request.size = page.itemsPerPage;

    this.listarTitulos();
  }

  filtrar(filter: FilterDTO) {
    this.request.filter = filter;
    this.listarTitulos();
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
    this.listarTitulos();
  }

  private getCurrencyForLocale(): string {
    const localeMap: Record<string, string> = {
      pt: 'BRL',
      'pt-BR': 'BRL',
      en: 'USD',
      'en-US': 'USD',
      es: 'EUR',
      'es-ES': 'EUR',
      fr: 'EUR',
      de: 'EUR',
    };
    return localeMap[this.locale] || 'USD';
  }
}
