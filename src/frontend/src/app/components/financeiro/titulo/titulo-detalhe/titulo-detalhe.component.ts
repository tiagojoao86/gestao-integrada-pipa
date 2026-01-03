import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { RouteConstants } from '../../../base/constants/route-constants';
import { TituloService } from '../titulo.service';
import { BaseComponent } from '../../../base/base.component';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';

import { IftaLabelModule } from 'primeng/iftalabel';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageModule } from 'primeng/message';
import { MessageService } from '../../../base/messages/messages.service';
import { TituloDTO } from '../model/titulo-dto';
import { AuthService } from '../../../base/auth/auth-service';
import {
  TituloSetorRateioComponent,
  TituloSetorRateio,
} from '../titulo-setor-rateio/titulo-setor-rateio.component';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { UsuarioUnidadeNegocioDTO } from '../../../cadastro/usuario/model/usuario-unidade-negocio-dto';

@Component({
  selector: 'gi-titulo-detalhe',
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    AutoCompleteModule,
    InputNumberModule,
    DatePickerModule,
    SelectModule,
    TextareaModule,
    IconFieldModule,
    InputIconModule,
    CheckboxModule,
    MessageModule,
    TituloSetorRateioComponent,
  ],
  templateUrl: './titulo-detalhe.component.html',
  styleUrl: './titulo-detalhe.component.css',
  providers: [TituloService],
})
export class TituloDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup([]);
  editMode = false;
  titulo: TituloDTO = {} as TituloDTO;
  @Input() id: string | number | null = null;
  @Output() backEvent = new EventEmitter<void>();

  private service: TituloService = inject(TituloService);
  private messages: MessageService = inject(MessageService);

  tituloTela = $localize`Título: `;

  // Autocomplete data
  allPessoas: { id: string; nome: string }[] = [];
  pessoaSuggestions: { id: string; nome: string }[] = [];
  pessoaInput: { id: string; nome: string } | null = null;

  // planoContas removed

  allUnidadesNegocio: UsuarioUnidadeNegocioDTO[] = [];

  // Categorias
  allCategorias: { id: string; codigo: string; nome: string }[] = [];

  // Setores para rateio
  setoresSelecionados: TituloSetorRateio[] = [];

  tiposOptions = [
    { label: $localize`A Pagar`, value: 'A_PAGAR' },
    { label: $localize`A Receber`, value: 'A_RECEBER' },
  ];

  statusOptions = [
    { label: $localize`Aberto`, value: 'ABERTO' },
    { label: $localize`Parcial`, value: 'PARCIAL' },
    { label: $localize`Pago`, value: 'PAGO' },
    { label: $localize`Cancelado`, value: 'CANCELADO' },
    { label: $localize`Vencido`, value: 'VENCIDO' },
  ];

  toolbarActions: ToolbarActionModel[] = [];
  private auth: AuthService = inject(AuthService);

  ngOnInit(): void {
    this.initForm();
    this.loadPessoas();
    this.loadUnidadesNegocio();
    this.loadCategorias();

    // Listen to unidadeNegocio changes (no planos de contas handling anymore)
    this.form.get('unidadeNegocio')?.valueChanges.subscribe(() => {
      // Intentionally left blank: plano de contas was removed from the UI/model
    });

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.FINANCEIRO_TITULO
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

    if (this.id === RouteConstants.P_ADD) {
      this.editMode = false;
      this.tituloTela += $localize`Novo`;
      this.titulo = {} as TituloDTO;
      this.form.get('tipo')?.setValue('A_PAGAR');
      // Status não é mais definido pelo usuário - será calculado pelo backend
      this.form.get('dataEmissao')?.setValue(new Date());
      // Load unidades and set default after loading
      this.loadUnidadesNegocio(true);
    } else {
      this.editMode = true;
      this.service.findById(String(this.id!)).subscribe((response) => {
        this.titulo = response.body!;
        this.tituloTela += this.titulo.descricao;
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = new FormBuilder().nonNullable;
    this.form.addControl('tipo', fb.control(null));
    this.form.addControl('status', fb.control(null));
    this.form.addControl('numeroDocumento', fb.control(null));
    this.form.addControl('descricao', fb.control(null));
    this.form.addControl('tituloCategoria', fb.control(null));
    this.form.addControl('valorOriginal', fb.control(null));
    this.form.addControl('valorDesconto', fb.control(null));
    this.form.addControl('valorJuros', fb.control(null));
    this.form.addControl('valorMulta', fb.control(null));
    this.form.addControl('dataEmissao', fb.control(null));
    this.form.addControl('dataVencimento', fb.control(null));
    this.form.addControl('dataPagamento', fb.control(null));
    this.form.addControl('observacoes', fb.control(null));
    this.form.addControl('unidadeNegocio', fb.control(''));
    this.form.addControl('rateioAutomatico', fb.control(false));
  }

  fillForm() {
    this.form.get('tipo')?.setValue(this.titulo.tipo);
    this.form.get('status')?.setValue(this.titulo.status);
    this.form.get('numeroDocumento')?.setValue(this.titulo.numeroDocumento);
    this.form.get('descricao')?.setValue(this.titulo.descricao);
    this.form
      .get('tituloCategoria')
      ?.setValue(this.titulo.tituloCategoriaId || null);
    this.form.get('valorOriginal')?.setValue(this.titulo.valorOriginal);
    this.form.get('valorDesconto')?.setValue(this.titulo.valorDesconto);
    this.form.get('valorJuros')?.setValue(this.titulo.valorJuros);
    this.form.get('valorMulta')?.setValue(this.titulo.valorMulta);
    this.form
      .get('dataEmissao')
      ?.setValue(
        this.titulo.dataEmissao ? new Date(this.titulo.dataEmissao) : null
      );
    this.form
      .get('dataVencimento')
      ?.setValue(
        this.titulo.dataVencimento ? new Date(this.titulo.dataVencimento) : null
      );
    this.form
      .get('dataPagamento')
      ?.setValue(
        this.titulo.dataPagamento ? new Date(this.titulo.dataPagamento) : null
      );
    this.form.get('observacoes')?.setValue(this.titulo.observacoes);
    this.form
      .get('unidadeNegocio')
      ?.setValue(this.titulo.unidadeNegocioId || '');
    this.form
      .get('rateioAutomatico')
      ?.setValue(this.titulo.rateioAutomatico || false);

    // Set autocomplete inputs
    if (this.titulo.pessoaId) {
      this.pessoaInput = {
        id: this.titulo.pessoaId,
        nome: this.titulo.pessoaNome || '',
      };
    }

    // Load setores
    if (this.titulo.setores && this.titulo.setores.length > 0) {
      this.setoresSelecionados = this.titulo.setores.map((s) => ({
        setorId: s.setorId,
        setorNome: s.setorNome || '',
        percentualRateio: Number(s.percentualRateio),
      }));
    }
    // planoContas removed
  }

  loadPessoas() {
    this.service.listarPessoasDisponiveis().subscribe((pessoas) => {
      this.allPessoas = pessoas;
    });
  }

  loadUnidadesNegocio(setDefault = false) {
    // First, load default unidade from auth cache to ensure it's available immediately
    const defaultUnidade = this.auth.getDefaultUnidadeNegocio();
    if (defaultUnidade) {
      this.allUnidadesNegocio = [defaultUnidade];
      // Set default immediately if needed
      if (setDefault && defaultUnidade.unidadeNegocioId) {
        this.form
          .get('unidadeNegocio')
          ?.setValue(defaultUnidade.unidadeNegocioId);
      }
    }

    // Then load all available unidades from backend

    this.allUnidadesNegocio = this.auth.getUnidadesNegocio();
    // Set default after backend load if needed and not already set
    if (
      setDefault &&
      defaultUnidade &&
      !this.form.get('unidadeNegocio')?.value
    ) {
      this.form
        .get('unidadeNegocio')
        ?.setValue(defaultUnidade.unidadeNegocioId);
    }
  }

  loadCategorias() {
    this.service.listarCategoriasDisponiveis().subscribe((categorias) => {
      this.allCategorias = categorias;
    });
  }

  searchPessoas(event: { query: string }) {
    const q = event.query ? String(event.query).toLowerCase() : '';
    this.pessoaSuggestions = this.allPessoas.filter((p) => {
      const nome = p?.nome ? String(p.nome).toLowerCase() : '';
      return nome.includes(q);
    });
  }

  onPessoaSelect(pessoa: { id: string; nome: string }) {
    this.titulo.pessoaId = pessoa.id;
    this.titulo.pessoaNome = pessoa.nome;
  }

  onSetoresChange(setores: TituloSetorRateio[]) {
    this.setoresSelecionados = setores;
  }

  get valorOriginalForRateio(): number {
    return this.form.value.valorOriginal || 0;
  }

  get setoresValidos(): boolean {
    if (this.setoresSelecionados.length === 0) {
      return false;
    }
    const soma = this.setoresSelecionados.reduce(
      (sum, s) => sum + (s.percentualRateio || 0),
      0
    );
    return Math.abs(soma - 100) < 0.01;
  }

  // planoContas related handlers removed

  salvar() {
    if (!this.form.valid) {
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    if (!this.titulo.pessoaId) {
      this.messages.erro($localize`Pessoa é obrigatória.`);
      return;
    }

    const unidadeNegocioId = this.form.value.unidadeNegocio;
    if (!unidadeNegocioId) {
      this.messages.erro($localize`Unidade de Negócio é obrigatória.`);
      return;
    }

    const tituloCategoriaId = this.form.value.tituloCategoria;
    if (!tituloCategoriaId) {
      this.messages.erro($localize`Categoria é obrigatória.`);
      return;
    }

    // Validação de setores
    if (!this.setoresValidos) {
      this.messages.erro(
        $localize`É necessário pelo menos um setor e a soma dos percentuais deve ser 100%.`
      );
      return;
    }

    this.titulo.unidadeNegocioId = unidadeNegocioId;
    this.titulo.tituloCategoriaId = tituloCategoriaId;
    this.titulo.tipo = this.form.value.tipo;
    // Status NÃO é enviado - será calculado pelo backend
    this.titulo.numeroDocumento = this.form.value.numeroDocumento;
    this.titulo.descricao = this.form.value.descricao;
    this.titulo.valorOriginal = this.form.value.valorOriginal;
    this.titulo.valorDesconto = this.form.value.valorDesconto || 0;
    this.titulo.valorJuros = this.form.value.valorJuros || 0;
    this.titulo.valorMulta = this.form.value.valorMulta || 0;
    this.titulo.dataEmissao = this.form.value.dataEmissao;
    this.titulo.dataVencimento = this.form.value.dataVencimento;
    this.titulo.dataPagamento = this.form.value.dataPagamento;
    this.titulo.observacoes = this.form.value.observacoes;
    this.titulo.rateioAutomatico = this.form.value.rateioAutomatico || false;

    // Map setores to DTO
    this.titulo.setores = this.setoresSelecionados.map((s) => ({
      setorId: s.setorId,
      setorNome: s.setorNome,
      percentualRateio: s.percentualRateio,
    }));

    this.service.save(this.titulo, {
      onSuccess: (data: TituloDTO) => {
        this.titulo = data;
        this.messages.sucesso($localize`Título salvo com sucesso.`);
        this.goBackFn();
      },
    });
  }

  isControlInvalid(campo: string) {
    const fc = this.form.get(campo);
    if (fc !== null && fc.invalid && (fc.touched || fc.dirty)) {
      return true;
    }
    return false;
  }

  goBackFn = () => {
    this.backEvent.emit();
  };
}
