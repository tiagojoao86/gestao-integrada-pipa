import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  OnDestroy,
  Output,
  LOCALE_ID,
} from '@angular/core';
import { Subject } from 'rxjs';
import { BaseComponent } from '../../../base/base.component';
import { MovimentacaoFinanceiraService } from '../movimentacao-financeira.service';
import { TituloService } from '../../titulo/titulo.service';
import { ContaBancariaService } from '../../conta-bancaria/conta-bancaria.service';
import { ContaBancariaDTO } from '../../conta-bancaria/model/conta-bancaria-dto';
import { Direction, PageRequest } from '../../../base/model/page-request';
import { AuthService } from '../../../base/auth/auth-service';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
  Validators,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { IftaLabelModule } from 'primeng/iftalabel';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { MessageService } from '../../../base/messages/messages.service';
import {
  MovimentacaoFinanceiraDTO,
  MovimentacaoTituloDTO,
} from '../model/movimentacao-financeira.dto';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { UsuarioUnidadeNegocioDTO } from '../../../cadastro/usuario/model/usuario-unidade-negocio-dto';
import {
  FilterDTO,
  FilterLogicOperator,
  FilterOperator,
} from '../../../base/model/filter-dto';
import { TableComponent } from '../../../base/table/table.component';
import { ColumnModel } from '../../../base/table/column.model';
import { LocaleUtils } from '../../../base/utils/locale-utils';
import { CurrencyPipe } from '@angular/common';
import { ActionModel } from '../../../base/table/action.model';
import { ButtonModule } from 'primeng/button';
import {
  EntitySearchConfig,
  ResultField,
  SearchField,
} from '../../../base/entity-search/entity-search.model';
import { TituloDTO } from '../../titulo/model/titulo-dto';
import { TituloGridDTO } from '../../titulo/model/titulo-grid-dto';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { DialogService } from '../../../base/dialog/dialog.service';

@Component({
  selector: 'gi-movimentacao-financeira-detalhe',
  templateUrl: './movimentacao-financeira-detalhe.component.html',
  styleUrls: ['./movimentacao-financeira-detalhe.component.css'],
  imports: [
    BaseComponent,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    InputNumberModule,
    DatePickerModule,
    SelectModule,
    TextareaModule,
    AutoCompleteModule,
    IftaLabelModule,
    IconFieldModule,
    InputIconModule,
    TableComponent,
    ButtonModule,
  ],
  providers: [
    MovimentacaoFinanceiraService,
    TituloService,
    ContaBancariaService,
    CurrencyPipe,
    EntitySearchService,
  ],
})
export class MovimentacaoFinanceiraDetalheComponent
  implements OnInit, OnDestroy
{
  @Input() id: string | null = null;
  @Input() titulosIniciais: TituloGridDTO[] = [];
  @Output() backEvent = new EventEmitter<void>();

  service: MovimentacaoFinanceiraService = inject(
    MovimentacaoFinanceiraService
  );
  messages: MessageService = inject(MessageService);
  tituloService: TituloService = inject(TituloService);
  authService: AuthService = inject(AuthService);
  currencyPipe: CurrencyPipe = inject(CurrencyPipe);
  contaService: ContaBancariaService = inject(ContaBancariaService);
  entitySearchService: EntitySearchService = inject(EntitySearchService);
  dialogService: DialogService = inject(DialogService);
  locale: string = inject(LOCALE_ID);

  tituloTela = $localize`Movimentação Financeira: `;
  form: FormGroup = new FormGroup({});
  editMode = false;
  movimentacao: MovimentacaoFinanceiraDTO = {} as MovimentacaoFinanceiraDTO;
  tiposOptions: { label: string; value: string }[] = [];
  formaOptions: { label: string; value: string }[] = [];
  toolbarActions: ToolbarActionModel[] = [];
  allUnidadesNegocio: UsuarioUnidadeNegocioDTO[] = [];
  contasOptions: ContaBancariaDTO[] = [];
  selectedTitulos: MovimentacaoTituloDTO[] = [];
  tituloColumns: ColumnModel<MovimentacaoTituloDTO>[] = [];
  tituloActions: ActionModel<MovimentacaoTituloDTO>[] = [];
  destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.populateTituloColumns();
    this.populateTituloActions();
    this.populateTiposTituloOptions();
    this.populateFormasPagamentoOptions();
    this.initForm();
    this.loadUnidadesNegocio();
    this.createToolbarActions();

    if (this.id === 'add') {
      this.prepareForNew();
    } else if (this.id) {
      this.prepareForEdit();
    }
  }

  populateTituloActions(): void {
    this.tituloActions = [
      {
        title: $localize`Remover`,
        icon: 'delete',
        action: (rowData: MovimentacaoTituloDTO) => {
          const index = this.selectedTitulos.findIndex(
            (it) => it.id === rowData.id
          );
          if (index != -1) {
            this.selectedTitulos.splice(index, 1);
            this.form.get('titulos')?.setValue([...this.selectedTitulos]);
            this.recalcularValorMovimentacao();
          }
        },
      },
    ];
  }

  populateTituloColumns(): void {
    this.tituloColumns = [
      {
        name: 'descricao',
        label: $localize`Descrição`,
        getValue(rowData: MovimentacaoTituloDTO) {
          return rowData.descricao;
        },
      },
      {
        name: 'valor',
        label: $localize`Saldo`,
        getValue: (rowData: MovimentacaoTituloDTO) => {
          const currency = LocaleUtils.getCurrencyForLocale(this.locale);
          return this.currencyPipe.transform(rowData.valor, currency, 'symbol');
        },
      },
    ];
  }

  populateTiposTituloOptions(): void {
    this.tiposOptions = [
      { label: $localize`Pagamento`, value: 'PAGAMENTO' },
      { label: $localize`Recebimento`, value: 'RECEBIMENTO' },
      { label: $localize`Estorno`, value: 'ESTORNO' },
      { label: $localize`Transferência`, value: 'TRANSFERENCIA' },
    ];
  }

  populateFormasPagamentoOptions(): void {
    this.formaOptions = [
      { label: 'PIX', value: 'PIX' },
      { label: $localize`Dinheiro`, value: 'DINHEIRO' },
      { label: $localize`Boleto`, value: 'BOLETO' },
      { label: $localize`Cartão de Crédito`, value: 'CARTAO_CREDITO' },
      { label: $localize`Cartão de Débito`, value: 'CARTAO_DEBITO' },
      { label: 'TED', value: 'TED' },
      { label: 'DOC', value: 'DOC' },
      { label: $localize`Cheque`, value: 'CHEQUE' },
      { label: $localize`Depósito`, value: 'DEPOSITO' },
    ];
  }

  loadUnidadesNegocio(): void {
    this.allUnidadesNegocio = this.authService.getUnidadesNegocio();
  }

  createToolbarActions(): void {
    const canEdit = this.authService.hasAuthorityEditarToModulo(
      SystemModuleKey.FINANCEIRO_MOVIMENTACAO_FINANCEIRA
    );
    this.toolbarActions = [
      {
        action: () => {
          this.goBackFn();
        },
        icon: 'close',
        title: $localize`Cancelar` + ' (esc)',
        shortcut: 'escape',
      },
    ];

    if (canEdit) {
      this.toolbarActions.push({
        action: () => {
          this.salvar();
        },
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });

      this.toolbarActions.unshift({
        action: () => {
          this.addTitulo();
        },
        icon: 'add_notes',
        title: $localize`Adicionar Título`,
      });
    }
  }

  prepareForNew(): void {
    this.editMode = false;
    this.movimentacao = {} as MovimentacaoFinanceiraDTO;
    this.form.get('data')?.setValue(new Date());
    const defaultUnidadeNegocio = this.authService.getDefaultUnidadeNegocio();
    if (defaultUnidadeNegocio) {
      this.form
        .get('unidadeNegocio')
        ?.setValue(defaultUnidadeNegocio.unidadeNegocioId);
      this.changeUnidadeNegocio();
    }
    if (this.titulosIniciais.length > 0) {
      this.selectedTitulos = this.titulosIniciais.map(
        (t) => new MovimentacaoTituloDTO(t.id, t.descricao, t.saldo, t.tipo)
      );
      this.form.get('titulos')?.setValue(this.selectedTitulos);
      this.recalcularValorMovimentacao();
    }
  }

  prepareForEdit(): void {
    this.editMode = true;
    this.service.findById(String(this.id)).subscribe((response) => {
      this.movimentacao = response.body!;
      this.fillForm();
      // Populate selectedTitulos from movimentacao DTO (includes valor por título)
      if (this.movimentacao?.titulos?.length > 0) {
        this.selectedTitulos = this.movimentacao.titulos.map(
          (t) => new MovimentacaoTituloDTO(t.id, t.descricao, t.valor, t.tipo)
        );
        this.form.get('titulos')?.setValue(this.selectedTitulos);
      }
    });
  }

  changeUnidadeNegocio() {
    this.contasOptions = [];
    const unidadeNegocioId = this.form.get('unidadeNegocio')?.value;

    if (unidadeNegocioId) {
      const filterItem = {
        property: 'unidadeNegocio',
        values: [unidadeNegocioId],
        operator: FilterOperator.EQ.key,
      };
      const filter = new FilterDTO(
        FilterLogicOperator.AND.getKey(),
        [filterItem],
        false
      );
      const order = {
        direction: Direction.ASC,
        property: 'nome',
      };
      const request = new PageRequest(filter, 0, 0, [order]);
      this.contaService.listAll(request).subscribe((response) => {
        this.contasOptions = response.body;
      });
    }
  }

  isControlInvalid(campo: string) {
    const fc = this.form.get(campo);
    if (fc !== null && fc.invalid && (fc.touched || fc.dirty)) {
      return true;
    }
    return false;
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('titulos', fb.control([], {
      validators: [(c) => (c.value as unknown[])?.length > 0 ? null : { required: true }],
    }));
    this.form.addControl('contaBancaria', fb.control('', [Validators.required]));
    this.form.addControl('tipo', fb.control(null, [Validators.required]));
    this.form.addControl('formaPagamento', fb.control('', [Validators.required]));
    this.form.addControl('valor', fb.control(null, [Validators.required, Validators.min(0.01)]));
    this.form.addControl('data', fb.control(null, [Validators.required]));
    this.form.addControl('unidadeNegocio', fb.control('', [Validators.required]));
    this.form.addControl('observacoes', fb.control(''));
  }

  fillForm() {
    this.selectedTitulos = [];

    this.form.get('titulos')?.setValue(this.selectedTitulos);
    this.form
      .get('contaBancaria')
      ?.setValue(
        this.movimentacao.contaBancariaId ||
          this.movimentacao.contaBancaria ||
          ''
      );
    this.form.get('tipo')?.setValue(this.movimentacao.tipo || null);
    this.form
      .get('formaPagamento')
      ?.setValue(this.movimentacao.formaPagamento || '');
    this.form.get('valor')?.setValue(this.movimentacao.valor || null);
    this.form
      .get('data')
      ?.setValue(
        this.movimentacao.data ? new Date(this.movimentacao.data) : null
      );
    this.form
      .get('unidadeNegocio')
      ?.setValue(this.movimentacao.unidadeNegocioId || '');
    this.form.get('observacoes')?.setValue(this.movimentacao.observacoes || '');
    this.changeUnidadeNegocio();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  salvar() {
    if (!this.validateBeforeSave()) return;
    this.populateDTOBeforeSend();

    this.service.save(this.movimentacao, {
      onSuccess: (data: MovimentacaoFinanceiraDTO) => {
        this.movimentacao = data;
        this.messages.sucesso($localize`Movimentação salva com sucesso.`);
        this.goBackFn();
      },
    });
  }

  validateBeforeSave(): boolean {
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return false;
    }

    const somaValoresTitulos = this.selectedTitulos.reduce(
      (sum, t) => sum + (t.valor || 0),
      0
    );
    const valorMovimentacao = this.form.value.valor || 0;
    if (Math.abs(valorMovimentacao - somaValoresTitulos) > 0.005) {
      this.messages.erro(
        $localize`O valor da movimentação deve ser igual à soma dos saldos dos títulos selecionados.`
      );
      return false;
    }

    return true;
  }

  recalcularValorMovimentacao(): void {
    const soma = this.selectedTitulos.reduce(
      (sum, t) => sum + (t.valor || 0),
      0
    );
    this.form.get('valor')?.setValue(soma > 0 ? soma : null);
  }

  buildTitulosToSend(): MovimentacaoTituloDTO[] {
    return (this.selectedTitulos || [])
      .filter((t) => !!t.id)
      .map((t) => new MovimentacaoTituloDTO(t.id, t.descricao, t.valor));
  }

  populateDTOBeforeSend() {
    // Send contaBancariaId as backend expects; keep legacy field in sync
    this.movimentacao.contaBancariaId = this.form.value.contaBancaria;
    this.movimentacao.contaBancaria = this.form.value.contaBancaria;
    this.movimentacao.tipo = this.form.value.tipo;
    this.movimentacao.formaPagamento = this.form.value.formaPagamento;
    this.movimentacao.valor = this.form.value.valor;
    this.movimentacao.data = this.form.value.data;
    this.movimentacao.unidadeNegocioId = this.form.value.unidadeNegocio;
    this.movimentacao.observacoes = this.form.value.observacoes;
    // Build DTO - filter out selected titles without id and send títulos as objects (backend expects MovimentacaoTituloDTO[])
    this.movimentacao.titulos = this.buildTitulosToSend();
  }

  goBackFn = () => {
    this.backEvent.emit();
  };

  addTitulo(): void {
    // Define os campos que podem ser pesquisados
    const searchFields: SearchField[] = [
      { key: 'numeroDocumento', label: $localize`Numero do Documento` },
      { key: 'descricao', label: $localize`Descrição` },
    ];

    // Define os campos que serão exibidos nos resultados
    const resultFields: ResultField[] = [
      { key: 'numeroDocumento', label: $localize`Numero do Documento` },
      { key: 'descricao', label: $localize`Descrição` },
    ];

    // Configura a busca
    const config: EntitySearchConfig<TituloDTO> = {
      service: this.tituloService,
      searchFields: searchFields,
      resultFields: resultFields,
      title: $localize`Pesquisar títulos`,
      searchPlaceholder: $localize`Digite o valor para pesquisar...`,
      pageSize: 10,
    };

    // Abre a modal e aguarda a seleção
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        if (this.tituloExists(result.entity.id)) {
          this.dialogService
            .showOk(
              $localize`Titulo já existe`,
              $localize`Titulo já está associado a essa movimentação`
            )
            .subscribe();
          return;
        }

        // Validar que o tipo do novo título é compatível com os já selecionados
        if (this.selectedTitulos.length > 0) {
          const tipoExistente = this.selectedTitulos[0].tipo;
          if (tipoExistente && result.entity.tipo !== tipoExistente) {
            const tipoLabel =
              tipoExistente === 'A_PAGAR'
                ? $localize`A Pagar`
                : $localize`A Receber`;
            this.dialogService
              .showOk(
                $localize`Tipo incompatível`,
                $localize`Não é possível misturar títulos A Pagar e A Receber. Os títulos selecionados são do tipo ${tipoLabel}.`
              )
              .subscribe();
            return;
          }
        }

        this.selectedTitulos.push({
          id: result.entity.id,
          descricao: result.entity.descricao,
          valor: result.entity.saldo,
          tipo: result.entity.tipo,
        });
        this.form.get('titulos')?.setValue([...this.selectedTitulos]);
        this.recalcularValorMovimentacao();
      }
    });
  }

  tituloExists(id: string | undefined): boolean {
    return this.selectedTitulos.filter((it) => it.id === id).length > 0;
  }
}
