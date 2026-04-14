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
import { SelectModule } from 'primeng/select';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageService } from '../../../base/messages/messages.service';
import { ProfissionalService } from '../profissional.service';
import { ProfissionalDTO } from '../model/profissional-dto';
import { TipoRemuneracao } from '../model/tipo-remuneracao.enum';
import { Conselho } from '../model/conselho.enum';
import { UF } from '../model/uf.enum';
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

@Component({
  selector: 'gi-profissional-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    SelectModule,
    CheckboxModule,
    EntityFieldComponent,
    PessoaDetalheComponent,
  ],
  templateUrl: './profissional-detalhe.component.html',
  styleUrl: './profissional-detalhe.component.css',
  providers: [ProfissionalService, PessoaService],
})
export class ProfissionalDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  editMode = false;
  profissional: ProfissionalDTO = new ProfissionalDTO();
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  pessoaSelecionada: PessoaDTO | null = null;
  readonly pessoaLabel = $localize`Profissional (Pessoa)`;

  tiposRemuneracao = TipoRemuneracao.getList();
  conselhos = Conselho.getList();
  ufs = UF.getList();

  titulo = $localize`Profissional: `;
  toolbarActions: ToolbarActionModel[] = [];
  canCadastrarPessoa = false;
  showPessoaDetalhe = false;

  private fb = inject(FormBuilder);
  private service = inject(ProfissionalService);
  private pessoaService = inject(PessoaService);
  private messages = inject(MessageService);

  readonly pessoaSearchConfig: EntitySearchConfig<PessoaDTO> = {
    service: this.pessoaService,
    searchFields: [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cpf', label: $localize`CPF` },
    ],
    resultFields: [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cpf', label: $localize`CPF` },
    ],
  };

  onPessoaAutoCompleteSelected(entity: unknown): void {
    const pessoa = entity as PessoaDTO;
    if (pessoa.tipoPessoa?.getKey() !== TipoPessoa.FISICA.getKey()) {
      this.messages.erro($localize`Apenas pessoas físicas podem ser cadastradas como profissional.`);
      return;
    }
    this.pessoaSelecionada = pessoa;
    this.form.get('pessoaId')?.setValue(pessoa.id);
  }
  private auth = inject(AuthService);
  private entitySearchService = inject(EntitySearchService);

  ngOnInit(): void {
    this.initForm();

    const canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.ATENDIMENTO_PROFISSIONAL
    );
    this.canCadastrarPessoa = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.CADASTRO_PESSOA
    );

    this.toolbarActions = [
      {
        action: () => this.goBackFn(),
        icon: 'close',
        title: $localize`Cancelar` + ' (esc)',
        shortcut: 'escape',
      },
    ];

    if (canEdit) {
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
        this.profissional = response.body!;
        this.titulo += this.profissional.pessoaNome;
        this.fillForm();
        this.form.get('pessoaId')?.disable();
      });
    }
  }

  initForm() {
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      pessoaId: fb.control('', [Validators.required]),
      conselho: fb.control<Conselho | null>(null, [Validators.required]),
      codigoConselho: fb.control('', [Validators.required, Validators.maxLength(30)]),
      uf: fb.control<UF | null>(null),
      tipoRemuneracao: fb.control<TipoRemuneracao | null>(null, [Validators.required]),
      banco: fb.control(''),
      conta: fb.control(''),
      chavePix: fb.control(''),
      ativo: fb.control(true),
    });
  }

  fillForm() {
    if (this.profissional.pessoaId) {
      this.form.get('pessoaId')?.setValue(this.profissional.pessoaId);
      this.pessoaSelecionada = {
        id: this.profissional.pessoaId,
        nome: this.profissional.pessoaNome,
      } as PessoaDTO;
    }
    this.form.get('conselho')?.setValue(Conselho.getByKey(this.profissional.conselho ?? '') ?? null);
    this.form.get('codigoConselho')?.setValue(this.profissional.codigoConselho ?? '');
    this.form.get('uf')?.setValue(this.profissional.uf ?? null);
    this.form.get('tipoRemuneracao')?.setValue(this.profissional.tipoRemuneracao ?? null);
    this.form.get('banco')?.setValue(this.profissional.banco ?? '');
    this.form.get('conta')?.setValue(this.profissional.conta ?? '');
    this.form.get('chavePix')?.setValue(this.profissional.chavePix ?? '');
    this.form.get('ativo')?.setValue(this.profissional.ativo ?? true);
  }

  pesquisarPessoa(): void {
    const searchFields: SearchField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cpf', label: $localize`CPF` },
    ];
    const resultFields: ResultField[] = [
      { key: 'nome', label: $localize`Nome` },
      { key: 'cpf', label: $localize`CPF` },
    ];
    const config: EntitySearchConfig<PessoaDTO> = {
      service: this.pessoaService,
      searchFields,
      resultFields,
      title: $localize`Selecionar Profissional`,
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
    if (pessoa.tipoPessoa !== TipoPessoa.FISICA.getKey()) {
      this.messages.erro($localize`Apenas pessoas físicas podem ser cadastradas como profissional.`);
      return;
    }
    this.pessoaSelecionada = { id: pessoa.id, nome: pessoa.nome } as PessoaDTO;
    this.form.get('pessoaId')?.setValue(pessoa.id);
  }

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const raw = this.form.getRawValue();
    this.profissional.pessoaId = raw.pessoaId;
    this.profissional.conselho = (raw.conselho as Conselho)?.getKey();
    this.profissional.codigoConselho = raw.codigoConselho;
    this.profissional.uf = (raw.uf as UF) || undefined;
    this.profissional.tipoRemuneracao = raw.tipoRemuneracao;
    this.profissional.banco = raw.banco || undefined;
    this.profissional.conta = raw.conta || undefined;
    this.profissional.chavePix = raw.chavePix || undefined;
    this.profissional.ativo = raw.ativo;

    this.service.save(this.profissional, {
      onSuccess: () => {
        this.messages.sucesso($localize`Profissional salvo com sucesso.`);
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
