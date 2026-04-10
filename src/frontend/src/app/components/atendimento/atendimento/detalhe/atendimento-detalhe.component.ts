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
import { TextareaModule } from 'primeng/textarea';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { MessageService } from '../../../base/messages/messages.service';
import { AtendimentoService } from '../atendimento.service';
import { AtendimentoDTO } from '../model/atendimento-dto';
import { StatusAtendimento } from '../model/status-atendimento.enum';
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
import { PessoaDTO } from '../../../cadastro/pessoa/model/pessoa-dto';
import { PessoaService } from '../../../cadastro/pessoa/pessoa.service';
import { SetorDTO } from '../../../cadastro/setor/model/setor-dto';
import { SetorService } from '../../../cadastro/setor/setor.service';
import { ProfissionalDTO } from '../../profissional/model/profissional-dto';
import { ProfissionalService } from '../../profissional/profissional.service';
import { ConvenioDTO } from '../../convenio/model/convenio-dto';
import { ConvenioService } from '../../convenio/convenio.service';
import { ConvenioCategoriaDTO } from '../../convenio-categoria/model/convenio-categoria-dto';
import { ConvenioCategoriaService } from '../../convenio-categoria/convenio-categoria.service';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { ProcedimentoService } from '../../procedimento/procedimento.service';

@Component({
  selector: 'gi-atendimento-detalhe',
  standalone: true,
  imports: [
    BaseComponent,
    IftaLabelModule,
    ReactiveFormsModule,
    FormsModule,
    InputTextModule,
    TextareaModule,
    SelectModule,
    DatePickerModule,
    EntityFieldComponent,
  ],
  templateUrl: './atendimento-detalhe.component.html',
  styleUrl: './atendimento-detalhe.component.css',
  providers: [
    AtendimentoService,
    PessoaService,
    SetorService,
    ProfissionalService,
    ConvenioService,
    ConvenioCategoriaService,
    ProcedimentoService,
  ],
})
export class AtendimentoDetalheComponent implements OnInit {
  form: FormGroup = new FormGroup({});
  editMode = false;
  atendimento: AtendimentoDTO = new AtendimentoDTO();
  @Input() detailId: string | number | null = null;
  @Output() closeDetail = new EventEmitter<void>();

  // Entity search selections
  setorSelecionado: SetorDTO | null = null;
  pacienteSelecionado: PessoaDTO | null = null;
  responsavelSelecionado: PessoaDTO | null = null;
  convenioSelecionado: ConvenioDTO | null = null;
  convenioCategoriaSelecionada: ConvenioCategoriaDTO | null = null;
  profissionalAtendimentoSelecionado: ProfissionalDTO | null = null;
  profissionalResponsavelSelecionado: ProfissionalDTO | null = null;
  procedimentoSelecionado: ProcedimentoDTO | null = null;

  readonly setorLabel = $localize`Setor`;
  readonly pacienteLabel = $localize`Paciente`;
  readonly responsavelLabel = $localize`Responsável`;
  readonly convenioLabel = $localize`Convênio`;
  readonly convenioCategoriaLabel = $localize`Categoria do Convênio`;
  readonly profAtendimentoLabel = $localize`Profissional do Atendimento`;
  readonly profResponsavelLabel = $localize`Profissional Responsável`;
  readonly procedimentoLabel = $localize`Procedimento`;

  statusOptions = StatusAtendimento.getAll();
  tabelaItemValorInfo: string | null = null;

  titulo = $localize`Atendimento: `;
  toolbarActions: ToolbarActionModel[] = [];
  canEdit = false;

  private fb = inject(FormBuilder);
  private service = inject(AtendimentoService);
  private pessoaService = inject(PessoaService);
  private setorService = inject(SetorService);
  private profissionalService = inject(ProfissionalService);
  private convenioService = inject(ConvenioService);
  private convenioCategoriaService = inject(ConvenioCategoriaService);
  private procedimentoService = inject(ProcedimentoService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);
  private entitySearchService = inject(EntitySearchService);

  ngOnInit(): void {
    this.initForm();

    this.canEdit = this.auth.hasAuthorityEditarToModulo(SystemModuleKey.ATENDIMENTO);

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

    if (this.detailId === RouteConstants.P_ADD) {
      this.editMode = false;
      this.titulo += $localize`Novo`;
      this.fillForm();
    } else {
      this.editMode = true;
      this.service.findById(String(this.detailId!)).subscribe((response) => {
        this.atendimento = response.body!;
        this.titulo += new Date(this.atendimento.dataHora!).toLocaleDateString('pt-BR');
        this.restoreSelections();
        this.fillForm();
      });
    }
  }

  initForm() {
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      dataHora: fb.control<Date | null>(null, [Validators.required]),
      setorId: fb.control('', [Validators.required]),
      pacienteId: fb.control('', [Validators.required]),
      responsavelId: fb.control(''),
      convenioId: fb.control(''),
      convenioCategoriaId: fb.control(''),
      profissionalAtendimentoId: fb.control('', [Validators.required]),
      profissionalResponsavelId: fb.control('', [Validators.required]),
      procedimentoId: fb.control('', [Validators.required]),
      status: fb.control<StatusAtendimento>(StatusAtendimento.AGENDADO, [Validators.required]),
      observacoes: fb.control(''),
    });
  }

  fillForm() {
    if (this.atendimento.dataHora) {
      this.form.get('dataHora')?.setValue(new Date(this.atendimento.dataHora));
    }
    this.form.get('status')?.setValue(this.atendimento.status ?? StatusAtendimento.AGENDADO);
    this.form.get('observacoes')?.setValue(this.atendimento.observacoes ?? '');
    this.form.get('setorId')?.setValue(this.atendimento.setorId ?? '');
    this.form.get('pacienteId')?.setValue(this.atendimento.pacienteId ?? '');
    this.form.get('responsavelId')?.setValue(this.atendimento.responsavelId ?? '');
    this.form.get('convenioId')?.setValue(this.atendimento.convenioId ?? '');
    this.form.get('convenioCategoriaId')?.setValue(this.atendimento.convenioCategoriaId ?? '');
    this.form.get('profissionalAtendimentoId')?.setValue(
      this.atendimento.profissionalAtendimentoId ?? '');
    this.form.get('profissionalResponsavelId')?.setValue(
      this.atendimento.profissionalResponsavelId ?? '');
    this.form.get('procedimentoId')?.setValue(this.atendimento.procedimentoId ?? '');

    if (this.atendimento.tabelaItemValor !== undefined && this.atendimento.tabelaItemValor !== null) {
      this.tabelaItemValorInfo = new Intl.NumberFormat('pt-BR', {
        style: 'currency', currency: 'BRL'
      }).format(this.atendimento.tabelaItemValor);
    }
  }

  restoreSelections() {
    if (this.atendimento.setorId) {
      this.setorSelecionado = { id: this.atendimento.setorId, nome: this.atendimento.setorNome } as SetorDTO;
    }
    if (this.atendimento.pacienteId) {
      this.pacienteSelecionado = {
        id: this.atendimento.pacienteId, nome: this.atendimento.pacienteNome
      } as PessoaDTO;
    }
    if (this.atendimento.responsavelId) {
      this.responsavelSelecionado = {
        id: this.atendimento.responsavelId, nome: this.atendimento.responsavelNome
      } as PessoaDTO;
    }
    if (this.atendimento.convenioId) {
      this.convenioSelecionado = {
        id: this.atendimento.convenioId, nome: this.atendimento.convenioNome
      } as ConvenioDTO;
    }
    if (this.atendimento.convenioCategoriaId) {
      this.convenioCategoriaSelecionada = {
        id: this.atendimento.convenioCategoriaId,
        nome: this.atendimento.convenioCategoriaNome
      } as ConvenioCategoriaDTO;
    }
    if (this.atendimento.profissionalAtendimentoId) {
      this.profissionalAtendimentoSelecionado = {
        id: this.atendimento.profissionalAtendimentoId,
        pessoaNome: this.atendimento.profissionalAtendimentoNome
      } as ProfissionalDTO;
    }
    if (this.atendimento.profissionalResponsavelId) {
      this.profissionalResponsavelSelecionado = {
        id: this.atendimento.profissionalResponsavelId,
        pessoaNome: this.atendimento.profissionalResponsavelNome
      } as ProfissionalDTO;
    }
    if (this.atendimento.procedimentoId) {
      this.procedimentoSelecionado = {
        id: this.atendimento.procedimentoId,
        codigo: this.atendimento.procedimentoCodigo,
        descricao: this.atendimento.procedimentoDescricao
      } as ProcedimentoDTO;
    }
  }

  // =========================================================================
  // Entity Search methods
  // =========================================================================

  pesquisarSetor(): void {
    const config: EntitySearchConfig<SetorDTO> = {
      service: this.setorService,
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [{ key: 'nome', label: $localize`Nome` }],
      title: $localize`Selecionar Setor`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.setorSelecionado = result.entity;
        this.form.get('setorId')?.setValue(result.entity.id);
      }
    });
  }

  pesquisarPaciente(): void {
    const config: EntitySearchConfig<PessoaDTO> = {
      service: this.pessoaService,
      searchFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'cpf', label: $localize`CPF` },
      ],
      resultFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'cpf', label: $localize`CPF` },
      ],
      title: $localize`Selecionar Paciente`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.pacienteSelecionado = result.entity;
        this.form.get('pacienteId')?.setValue(result.entity.id);
        // Auto-fill responsavel se paciente tiver responsavel
        if (result.entity.responsavelId && !this.responsavelSelecionado) {
          this.responsavelSelecionado = {
            id: result.entity.responsavelId,
            nome: result.entity.responsavelNome
          } as PessoaDTO;
          this.form.get('responsavelId')?.setValue(result.entity.responsavelId);
        }
      }
    });
  }

  pesquisarResponsavel(): void {
    const config: EntitySearchConfig<PessoaDTO> = {
      service: this.pessoaService,
      searchFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'cpf', label: $localize`CPF` },
      ],
      resultFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'cpf', label: $localize`CPF` },
      ],
      title: $localize`Selecionar Responsável`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.responsavelSelecionado = result.entity;
        this.form.get('responsavelId')?.setValue(result.entity.id);
      }
    });
  }

  pesquisarConvenio(): void {
    const config: EntitySearchConfig<ConvenioDTO> = {
      service: this.convenioService,
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [{ key: 'nome', label: $localize`Nome` }],
      title: $localize`Selecionar Convênio`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.convenioSelecionado = result.entity;
        this.form.get('convenioId')?.setValue(result.entity.id);
        // Limpar categoria ao trocar convênio
        this.limparConvenioCategoria();
      }
    });
  }

  pesquisarConvenioCategoria(): void {
    if (!this.convenioSelecionado) {
      this.messages.erro($localize`Selecione um convênio primeiro.`);
      return;
    }
    const config: EntitySearchConfig<ConvenioCategoriaDTO> = {
      service: this.convenioCategoriaService,
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'convenioNome', label: $localize`Convênio` },
      ],
      title: $localize`Selecionar Categoria do Convênio`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.convenioCategoriaSelecionada = result.entity;
        this.form.get('convenioCategoriaId')?.setValue(result.entity.id);
      }
    });
  }

  pesquisarProfissionalAtendimento(): void {
    const config: EntitySearchConfig<ProfissionalDTO> = {
      service: this.profissionalService,
      searchFields: [{ key: 'pessoaNome', label: $localize`Nome` }],
      resultFields: [{ key: 'pessoaNome', label: $localize`Nome` }],
      title: $localize`Selecionar Profissional do Atendimento`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.profissionalAtendimentoSelecionado = result.entity;
        this.form.get('profissionalAtendimentoId')?.setValue(result.entity.id);
      }
    });
  }

  pesquisarProfissionalResponsavel(): void {
    const config: EntitySearchConfig<ProfissionalDTO> = {
      service: this.profissionalService,
      searchFields: [{ key: 'pessoaNome', label: $localize`Nome` }],
      resultFields: [{ key: 'pessoaNome', label: $localize`Nome` }],
      title: $localize`Selecionar Profissional Responsável`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.profissionalResponsavelSelecionado = result.entity;
        this.form.get('profissionalResponsavelId')?.setValue(result.entity.id);
      }
    });
  }

  pesquisarProcedimento(): void {
    const config: EntitySearchConfig<ProcedimentoDTO> = {
      service: this.procedimentoService,
      searchFields: [
        { key: 'codigo', label: $localize`Código` },
        { key: 'descricao', label: $localize`Descrição` },
      ],
      resultFields: [
        { key: 'codigo', label: $localize`Código` },
        { key: 'descricao', label: $localize`Descrição` },
      ],
      title: $localize`Selecionar Procedimento`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.procedimentoSelecionado = result.entity;
        this.form.get('procedimentoId')?.setValue(result.entity.id);
      }
    });
  }

  // =========================================================================
  // Limpar seleções
  // =========================================================================

  limparSetor(): void { this.setorSelecionado = null; this.form.get('setorId')?.setValue(''); }
  limparPaciente(): void {
    this.pacienteSelecionado = null;
    this.form.get('pacienteId')?.setValue('');
  }
  limparResponsavel(): void {
    this.responsavelSelecionado = null;
    this.form.get('responsavelId')?.setValue('');
  }
  limparConvenio(): void {
    this.convenioSelecionado = null;
    this.form.get('convenioId')?.setValue('');
    this.limparConvenioCategoria();
  }
  limparConvenioCategoria(): void {
    this.convenioCategoriaSelecionada = null;
    this.form.get('convenioCategoriaId')?.setValue('');
  }
  limparProfissionalAtendimento(): void {
    this.profissionalAtendimentoSelecionado = null;
    this.form.get('profissionalAtendimentoId')?.setValue('');
  }
  limparProfissionalResponsavel(): void {
    this.profissionalResponsavelSelecionado = null;
    this.form.get('profissionalResponsavelId')?.setValue('');
  }
  limparProcedimento(): void {
    this.procedimentoSelecionado = null;
    this.form.get('procedimentoId')?.setValue('');
  }

  // =========================================================================

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }

    const raw = this.form.getRawValue();
    const dataHora: Date = raw.dataHora;
    this.atendimento.dataHora = dataHora ? dataHora.toISOString() : undefined;
    this.atendimento.setorId = raw.setorId || undefined;
    this.atendimento.pacienteId = raw.pacienteId || undefined;
    this.atendimento.responsavelId = raw.responsavelId || undefined;
    this.atendimento.convenioId = raw.convenioId || undefined;
    this.atendimento.convenioCategoriaId = raw.convenioCategoriaId || undefined;
    this.atendimento.profissionalAtendimentoId = raw.profissionalAtendimentoId || undefined;
    this.atendimento.profissionalResponsavelId = raw.profissionalResponsavelId || undefined;
    this.atendimento.procedimentoId = raw.procedimentoId || undefined;
    this.atendimento.status = raw.status;
    this.atendimento.observacoes = raw.observacoes || undefined;

    this.service.save(this.atendimento, {
      onSuccess: () => {
        this.messages.sucesso($localize`Atendimento salvo com sucesso.`);
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
