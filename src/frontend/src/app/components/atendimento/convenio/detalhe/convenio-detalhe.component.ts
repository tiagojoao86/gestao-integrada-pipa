import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { BaseComponent } from '../../../base/base.component';
import { IftaLabelModule } from 'primeng/iftalabel';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
  Validators,
} from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { MessageService } from '../../../base/messages/messages.service';
import { ConvenioService } from '../convenio.service';
import { ConvenioDTO } from '../model/convenio-dto';
import { CodigoConvenioDTO } from '../model/codigo-convenio-dto';
import { ConvenioTipoCobranca, convenioTipoCobrancaLabel } from '../model/convenio-tipo-cobranca.enum';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { AuthService } from '../../../base/auth/auth-service';
import { RouteConstants } from '../../../base/constants/route-constants';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import {
  EntitySearchConfig,
  SearchField,
  ResultField,
} from '../../../base/entity-search/entity-search.model';
import { EntityFieldComponent } from '../../../base/entity-field/entity-field.component';
import { PessoaDTO, TipoPessoa } from '../../../cadastro/pessoa/model/pessoa-dto';
import { PessoaService } from '../../../cadastro/pessoa/pessoa.service';
import { PessoaDetalheComponent } from '../../../cadastro/pessoa/pessoa-detalhe/pessoa-detalhe.component';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { ProcedimentoService } from '../../procedimento/procedimento.service';

@Component({
  selector: 'gi-convenio-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    CheckboxModule,
    TableModule,
    ButtonModule,
    SelectModule,
    EntityFieldComponent,
    PessoaDetalheComponent,
  ],
  templateUrl: './convenio-detalhe.component.html',
  styleUrl: './convenio-detalhe.component.css',
  providers: [ConvenioService, PessoaService, ProcedimentoService],
})
export class ConvenioDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  editMode = false;
  convenio: ConvenioDTO = new ConvenioDTO();
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();
  @Output() convenioSalvo = new EventEmitter<{ id: string; nome: string }>();

  pessoaSelecionada: PessoaDTO | null = null;
  readonly pessoaLabel = $localize`Pessoa (CNPJ/Razão Social)`;

  readonly tipoCobrancaOptions = [
    { label: convenioTipoCobrancaLabel(ConvenioTipoCobranca.FATURADO), value: ConvenioTipoCobranca.FATURADO },
    { label: convenioTipoCobrancaLabel(ConvenioTipoCobranca.PAGO_NO_ATO), value: ConvenioTipoCobranca.PAGO_NO_ATO },
  ];

  // Gestão inline de CodigoConvenio
  codigos: CodigoConvenioDTO[] = [];
  procedimentoSelecionado: ProcedimentoDTO | null = null;
  codigoInputTemp = '';
  readonly procedimentoLabel = $localize`Procedimento`;

  titulo = $localize`Convênio: `;
  toolbarActions: ToolbarActionModel[] = [];
  canEdit = false;
  canCadastrarPessoa = false;
  showPessoaDetalhe = false;

  private fb = inject(FormBuilder);
  private service = inject(ConvenioService);
  private pessoaService = inject(PessoaService);
  private procedimentoService = inject(ProcedimentoService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);
  private entitySearchService = inject(EntitySearchService);

  readonly pessoaSearchConfig: EntitySearchConfig<PessoaDTO> = {
    service: this.pessoaService,
    searchFields: [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cnpj', label: $localize`CNPJ` },
    ],
    resultFields: [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cnpj', label: $localize`CNPJ` },
    ],
  };

  onPessoaAutoCompleteSelected(entity: unknown): void {
    const pessoa = entity as PessoaDTO;
    if (pessoa.tipoPessoa?.getKey() !== TipoPessoa.JURIDICA.getKey()) {
      this.messages.erro($localize`Apenas pessoas jurídicas podem ser cadastradas como convênio.`);
      return;
    }
    this.pessoaSelecionada = pessoa;
    this.form.get('pessoaId')?.setValue(pessoa.id);
  }

  ngOnInit(): void {
    this.initForm();

    this.canEdit = this.auth.hasAuthorityEditarToModulo(SystemModuleKey.ATENDIMENTO_CONVENIO);
    this.canCadastrarPessoa = this.auth.hasAuthorityEditarToModulo(SystemModuleKey.CADASTRO_PESSOA);

    this.toolbarActions = [
      { action: () => this.goBackFn(), icon: 'close', title: $localize`Cancelar` + ' (esc)', shortcut: 'escape' },
    ];

    if (this.canEdit) {
      this.toolbarActions.push({
        action: () => this.save(),
        icon: 'save',
        title: $localize`Salvar` + ' (enter)',
        shortcut: 'enter',
      });
    }

    if (this.canCadastrarPessoa && this.detailId === RouteConstants.P_ADD) {
      this.toolbarActions.unshift({
        action: () => this.abrirCadastroPessoa(),
        icon: 'person_add',
        title: $localize`Cadastrar nova pessoa`,
      });
    }

    if (this.detailId === RouteConstants.P_ADD) {
      this.editMode = false;
      this.titulo += $localize`Novo`;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.convenio = response.body!;
        this.titulo += this.convenio.nome;
        this.codigos = this.convenio.codigos ? [...this.convenio.codigos] : [];
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      nome: fb.control('', [Validators.required, Validators.maxLength(100)]),
      tipoCobranca: fb.control(ConvenioTipoCobranca.FATURADO, [Validators.required]),
      pessoaId: fb.control(''),
      registroAns: fb.control('', [Validators.maxLength(20)]),
      ativo: fb.control(true),
    });

    this.form.get('tipoCobranca')!.valueChanges.subscribe(() => this.atualizarValidacaoPessoa());
    this.atualizarValidacaoPessoa();
  }

  private atualizarValidacaoPessoa(): void {
    const tipoCobranca = this.form.get('tipoCobranca')?.value;
    const pessoaControl = this.form.get('pessoaId')!;
    if (tipoCobranca === ConvenioTipoCobranca.FATURADO) {
      pessoaControl.addValidators(Validators.required);
    } else {
      pessoaControl.removeValidators(Validators.required);
      pessoaControl.setValue('');
      this.pessoaSelecionada = null;
    }
    pessoaControl.updateValueAndValidity();
  }

  fillForm() {
    this.form.get('nome')?.setValue(this.convenio.nome ?? '');
    this.form.get('tipoCobranca')?.setValue(this.convenio.tipoCobranca ?? ConvenioTipoCobranca.FATURADO);
    this.form.get('registroAns')?.setValue(this.convenio.registroAns ?? '');
    this.form.get('ativo')?.setValue(this.convenio.ativo ?? true);
    if (this.convenio.pessoaId) {
      this.form.get('pessoaId')?.setValue(this.convenio.pessoaId);
      this.pessoaSelecionada = { id: this.convenio.pessoaId, nome: this.convenio.pessoaNome } as PessoaDTO;
    }
  }

  pesquisarPessoa(): void {
    const searchFields: SearchField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cnpj', label: $localize`CNPJ` },
    ];
    const resultFields: ResultField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cnpj', label: $localize`CNPJ` },
    ];
    const config: EntitySearchConfig<PessoaDTO> = {
      service: this.pessoaService,
      searchFields,
      resultFields,
      title: $localize`Selecionar Convênio (Pessoa)`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.pessoaSelecionada = result.entity;
        this.form.get('pessoaId')?.setValue(result.entity.id);
      }
    });
  }

  limparPessoa(): void {
    this.pessoaSelecionada = null;
    this.form.get('pessoaId')?.setValue('');
  }

  abrirCadastroPessoa(): void {
    this.showPessoaDetalhe = true;
  }

  fecharPessoaDetalhe(): void {
    this.showPessoaDetalhe = false;
  }

  onPessoaSalva(pessoa: { id: string; nome: string; tipoPessoa: string }): void {
    if (pessoa.tipoPessoa !== TipoPessoa.JURIDICA.getKey()) {
      this.messages.erro($localize`Apenas pessoas jurídicas podem ser cadastradas como convênio.`);
      return;
    }
    this.pessoaSelecionada = { id: pessoa.id, nome: pessoa.nome } as PessoaDTO;
    this.form.get('pessoaId')?.setValue(pessoa.id);
  }

  // =========================================================================
  // Gestão inline de CodigoConvenio
  // =========================================================================

  pesquisarProcedimento(): void {
    const searchFields: SearchField[] = [
      { key: 'codigo', label: $localize`Código` },
      { key: 'descricao', label: $localize`Descrição` },
    ];
    const resultFields: ResultField[] = [
      { key: 'codigo', label: $localize`Código` },
      { key: 'descricao', label: $localize`Descrição` },
    ];
    const config: EntitySearchConfig<ProcedimentoDTO> = {
      service: this.procedimentoService,
      searchFields,
      resultFields,
      title: $localize`Selecionar Procedimento`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.procedimentoSelecionado = result.entity;
      }
    });
  }

  limparProcedimento(): void {
    this.procedimentoSelecionado = null;
    this.codigoInputTemp = '';
  }

  adicionarCodigo(): void {
    if (!this.procedimentoSelecionado || !this.codigoInputTemp.trim()) {
      this.messages.erro($localize`Selecione um procedimento e informe o código.`);
      return;
    }

    const jaExiste = this.codigos.some(
      (c) => c.procedimentoId === this.procedimentoSelecionado!.id
    );
    if (jaExiste) {
      this.messages.erro($localize`Este procedimento já possui um código cadastrado.`);
      return;
    }

    const novo: CodigoConvenioDTO = new CodigoConvenioDTO();
    novo.procedimentoId = this.procedimentoSelecionado.id;
    novo.procedimentoCodigo = this.procedimentoSelecionado.codigo;
    novo.procedimentoDescricao = this.procedimentoSelecionado.descricao;
    novo.codigo = this.codigoInputTemp.trim();

    this.codigos = [...this.codigos, novo];
    this.limparProcedimento();
  }

  removerCodigo(index: number): void {
    this.codigos = this.codigos.filter((_, i) => i !== index);
  }

  // =========================================================================

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const raw = this.form.getRawValue();
    this.convenio.nome = raw.nome;
    this.convenio.tipoCobranca = raw.tipoCobranca;
    this.convenio.pessoaId = raw.pessoaId || undefined;
    this.convenio.registroAns = raw.registroAns || undefined;
    this.convenio.ativo = raw.ativo;
    this.convenio.codigos = this.codigos;

    this.service.save(this.convenio, {
      onSuccess: (data: ConvenioDTO) => {
        this.messages.sucesso($localize`Convênio salvo com sucesso.`);
        this.convenioSalvo.emit({ id: data.id!, nome: data.nome ?? '' });
        this.goBackFn();
      },
    });
  }

  isControlInvalid(campo: string): boolean {
    const fc: AbstractControl | null = this.form.get(campo);
    return fc !== null && fc.invalid && (fc.touched || fc.dirty);
  }

  goBackFn = () => {
    this.closeDetail.emit();
  };
}
