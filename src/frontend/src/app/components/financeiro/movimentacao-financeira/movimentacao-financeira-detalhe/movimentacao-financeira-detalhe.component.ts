import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  OnDestroy,
  Output,
} from '@angular/core';
import { Subject, of, forkJoin } from 'rxjs';
import {
  debounceTime,
  distinctUntilChanged,
  switchMap,
  takeUntil,
  catchError,
  map,
  take,
} from 'rxjs/operators';
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
import { TituloDTO } from '../../titulo/model/titulo-dto';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { UsuarioUnidadeNegocioDTO } from '../../../cadastro/usuario/model/usuario-unidade-negocio-dto';
import {
  FilterDTO,
  FilterLogicOperator,
  FilterOperator,
} from '../../../base/model/filter-dto';

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
  ],
  providers: [
    MovimentacaoFinanceiraService,
    TituloService,
    ContaBancariaService,
  ],
})
export class MovimentacaoFinanceiraDetalheComponent
  implements OnInit, OnDestroy
{
  tituloTela = $localize`Movimentação Financeira: `;
  form: FormGroup = new FormGroup({});
  editMode = false;
  movimentacao: MovimentacaoFinanceiraDTO = {} as MovimentacaoFinanceiraDTO;
  @Input() id: string | null = null;
  @Output() backEvent = new EventEmitter<void>();

  private service: MovimentacaoFinanceiraService = inject(
    MovimentacaoFinanceiraService
  );
  private messages: MessageService = inject(MessageService);

  tituloSuggestions: TituloDTO[] = [];
  selectedTitulos: TituloDTO[] = [];

  tiposOptions = [
    { label: $localize`Pagamento`, value: 'PAGAMENTO' },
    { label: $localize`Recebimento`, value: 'RECEBIMENTO' },
    { label: $localize`Estorno`, value: 'ESTORNO' },
    { label: $localize`Transferência`, value: 'TRANSFERENCIA' },
  ];

  // Forma de pagamento options (mirror backend enum)
  formaOptions = [
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

  toolbarActions: ToolbarActionModel[] = [];

  allUnidadesNegocio: UsuarioUnidadeNegocioDTO[] = [];

  private tituloService: TituloService = inject(TituloService);
  private authService: AuthService = inject(AuthService);
  private searchSubjectTitulos = new Subject<string>();
  private destroy$ = new Subject<void>();
  private contaService: ContaBancariaService = inject(ContaBancariaService);
  contasOptions: ContaBancariaDTO[] = [];

  ngOnInit(): void {
    this.initForm();
    this.createSearchSubjectTitulos();
    this.loadUnidadesNegocio();
    this.createToolbarActions();

    if (this.id === 'add') {
      this.prepareForNew();
    } else if (this.id) {
      this.prepareForEdit();
    }
  }

  createSearchSubjectTitulos(): void {
    this.searchSubjectTitulos
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((q) =>
          q && q.length > 0
            ? this.tituloService.search(q, 10).pipe(catchError(() => of([])))
            : of([])
        ),
        takeUntil(this.destroy$)
      )
      .subscribe((results) => {
        this.tituloSuggestions = results as TituloDTO[];
      });
  }

  loadUnidadesNegocio(): void {
    this.allUnidadesNegocio = this.authService.getUnidadesNegocio();
  }

  createToolbarActions(): void {
    // Configure action toolbar (Cancelar / Salvar) based on permissions
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
  }

  prepareForEdit(): void {
    this.editMode = true;
    this.service.findById(String(this.id)).subscribe((response) => {
      this.movimentacao = response.body!;
      this.fillForm();
      // If there are existing títulos, extract ids and fetch them to populate selection
      if (
        this.movimentacao &&
        this.movimentacao.titulos &&
        this.movimentacao.titulos.length > 0
      ) {
        const ids = this.movimentacao.titulos
          .map((t: string | MovimentacaoTituloDTO) =>
            typeof t === 'string' ? t : t.id
          )
          .filter((id) => !!id);
        const calls = ids.map((id) =>
          this.tituloService.findById(id as string).pipe(map((r) => r.body))
        );
        if (calls.length > 0) {
          forkJoin(calls)
            .pipe(take(1))
            .subscribe((arr) => {
              this.selectedTitulos = arr as TituloDTO[];
              this.form.get('titulos')?.setValue(this.selectedTitulos);
            });
        }
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
    this.form.addControl('titulos', fb.control([]));
    this.form.addControl('contaBancaria', fb.control(''));
    this.form.addControl('tipo', fb.control(null));
    this.form.addControl('formaPagamento', fb.control(''));
    this.form.addControl('valor', fb.control(null));
    this.form.addControl('data', fb.control(null));
    this.form.addControl('unidadeNegocio', fb.control(''));
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

  searchTitulos(event: { query: string }) {
    const q = event && event.query ? String(event.query) : '';
    this.searchSubjectTitulos.next(q);
  }

  // PrimeNG AutoComplete onChange emits either an array of items or an object like { originalEvent, value }
  onTitulosChange(evt: TituloDTO[] | { value?: TituloDTO[] }) {
    // Cases to handle:
    // - evt is an array (full selection) -> replace selectedTitulos
    // - evt is an object { value: TituloDTO[] } -> replace selectedTitulos
    // - evt is a single TituloDTO (onSelect) -> add to selectedTitulos if not present
    if (Array.isArray(evt)) {
      this.selectedTitulos = evt;
    } else if (evt && 'value' in evt && Array.isArray(evt.value)) {
      this.selectedTitulos = evt.value as TituloDTO[];
    } else if (evt && typeof evt === 'object' && 'id' in evt) {
      const item = evt as unknown as TituloDTO;
      const exists = this.selectedTitulos.some((t) => t.id === item.id);
      if (!exists) this.selectedTitulos = [...this.selectedTitulos, item];
    } else {
      // fallback: do nothing
      return;
    }

    this.form.get('titulos')?.setValue(this.selectedTitulos);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const titulosIds = (this.selectedTitulos || [])
      .map((t) => t.id)
      .filter((id) => !!id);
    if (titulosIds.length === 0) {
      this.messages.erro($localize`Pelo menos um título é obrigatório.`);
      return;
    }

    if (!this.form.value.contaBancaria) {
      this.messages.erro($localize`Conta bancária é obrigatória.`);
      return;
    }

    if (!this.form.value.tipo) {
      this.messages.erro($localize`Tipo de movimentação é obrigatório.`);
      return;
    }

    if (!this.form.value.formaPagamento) {
      this.messages.erro($localize`Forma de pagamento é obrigatória.`);
      return;
    }

    const valor = this.form.value.valor;
    if (!valor || valor <= 0) {
      this.messages.erro($localize`Valor deve ser maior que zero.`);
      return;
    }

    const data = this.form.value.data;
    if (!data) {
      this.messages.erro($localize`Data é obrigatória.`);
      return;
    }

    if (!this.form.value.unidadeNegocio) {
      this.messages.erro($localize`Unidade de negócio é obrigatória.`);
      return;
    }

    // Build DTO - filter out selected titles without id and send títulos as objects (backend expects MovimentacaoTituloDTO[])
    const titulosToSend = (this.selectedTitulos || [])
      .filter((t) => !!t.id)
      .map((t) => ({ id: t.id, descricao: t.descricao }));
    this.movimentacao.titulos = titulosToSend;
    // Send contaBancariaId as backend expects; keep legacy field in sync
    this.movimentacao.contaBancariaId = this.form.value.contaBancaria;
    this.movimentacao.contaBancaria = this.form.value.contaBancaria;
    this.movimentacao.tipo = this.form.value.tipo;
    this.movimentacao.formaPagamento = this.form.value.formaPagamento;
    this.movimentacao.valor = this.form.value.valor;
    this.movimentacao.data = this.form.value.data;
    this.movimentacao.unidadeNegocioId =
      this.form.value.unidadeNegocio.unidadeNegocioId;
    this.movimentacao.observacoes = this.form.value.observacoes;

    this.service.save(this.movimentacao, {
      onSuccess: (data: MovimentacaoFinanceiraDTO) => {
        this.movimentacao = data;
        this.messages.sucesso($localize`Movimentação salva com sucesso.`);
        this.goBackFn();
      },
    });
  }

  goBackFn = () => {
    this.backEvent.emit();
  };
}
