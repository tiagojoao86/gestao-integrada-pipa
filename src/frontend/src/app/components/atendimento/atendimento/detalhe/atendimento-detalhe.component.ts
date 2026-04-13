import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
  OnDestroy,
} from '@angular/core';
import { Subscription } from 'rxjs';
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
import { DatePickerModule } from 'primeng/datepicker';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { MessageService } from '../../../base/messages/messages.service';
import { AtendimentoService } from '../atendimento.service';
import { AtendimentoDTO } from '../model/atendimento-dto';
import { AtendimentoProcedimentoDTO } from '../model/atendimento-procedimento-dto';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { AuthService } from '../../../base/auth/auth-service';
import { RouteConstants } from '../../../base/constants/route-constants';
import { SystemModuleKey } from '../../../base/enum/system-module-key.enum';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { EntitySearchConfig } from '../../../base/entity-search/entity-search.model';
import { EntityFieldComponent } from '../../../base/entity-field/entity-field.component';
import { PessoaDTO } from '../../../cadastro/pessoa/model/pessoa-dto';
import { PessoaService } from '../../../cadastro/pessoa/pessoa.service';
import { SetorDTO } from '../../../cadastro/setor/model/setor-dto';
import { SetorService } from '../../../cadastro/setor/setor.service';
import { ProfissionalDTO } from '../../profissional/model/profissional-dto';
import { ProfissionalService } from '../../profissional/profissional.service';
import { ConvenioDTO } from '../../convenio/model/convenio-dto';
import { ConvenioService } from '../../convenio/convenio.service';
import { ConvenioCategoriaGridDTO } from '../../convenio-categoria/model/convenio-categoria-grid-dto';
import { ConvenioCategoriaService } from '../../convenio-categoria/convenio-categoria.service';
import { ProcedimentoDTO } from '../../procedimento/model/procedimento-dto';
import { ProcedimentoService } from '../../procedimento/procedimento.service';

interface ProcedimentoLinha {
  procedimentoId?: string;
  procedimentoCodigo?: string;
  procedimentoDescricao?: string;
  convenioId?: string;
  convenioNome?: string;
  tabelaItemId?: string;
  tabelaItemValor?: number;
  dataInicio: Date;
  dataFim: Date;
}

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
    DatePickerModule,
    TableModule,
    ButtonModule,
    SelectModule,
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
export class AtendimentoDetalheComponent implements OnInit, OnDestroy {
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
  profissionalAtendimentoSelecionado: ProfissionalDTO | null = null;
  profissionalResponsavelSelecionado: ProfissionalDTO | null = null;

  // Categorias do convênio selecionado
  categoriasOptions: ConvenioCategoriaGridDTO[] = [];

  private subs = new Subscription();

  // Lista de procedimentos
  procedimentos: ProcedimentoLinha[] = [];

  readonly setorLabel = $localize`Setor`;
  readonly pacienteLabel = $localize`Paciente`;
  readonly responsavelLabel = $localize`Responsável`;
  readonly convenioLabel = $localize`Convênio`;
  readonly profAtendimentoLabel = $localize`Profissional do Atendimento`;
  readonly profResponsavelLabel = $localize`Profissional Responsável`;
  readonly addProcedimentoLabel = $localize`Adicionar Procedimento`;

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
    this.watchDataInicio();

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
        this.titulo += new Date(this.atendimento.dataInicio!).toLocaleDateString('pt-BR');
        this.restoreSelections();
        this.fillForm();
      });
    }
  }

  initForm() {
    const now = new Date();
    const fb = this.fb.nonNullable;
    this.form = fb.group({
      dataInicio: fb.control<Date | null>(now, [Validators.required]),
      dataFim: fb.control<Date | null>(this.endOfDay(now), [Validators.required]),
      setorId: fb.control('', [Validators.required]),
      pacienteId: fb.control('', [Validators.required]),
      responsavelId: fb.control(''),
      convenioId: fb.control(''),
      convenioCategoriaId: fb.control({ value: '', disabled: true }),
      profissionalAtendimentoId: fb.control('', [Validators.required]),
      profissionalResponsavelId: fb.control('', [Validators.required]),
      observacoes: fb.control(''),
    });
  }

  watchDataInicio() {
    this.form.get('dataInicio')?.valueChanges.subscribe((val: Date | null) => {
      if (val) {
        this.form.get('dataFim')?.setValue(this.endOfDay(val), { emitEvent: false });
      }
    });
  }

  fillForm() {
    if (this.atendimento.dataInicio) {
      this.form.get('dataInicio')?.setValue(new Date(this.atendimento.dataInicio), { emitEvent: false });
    }
    if (this.atendimento.dataFim) {
      this.form.get('dataFim')?.setValue(new Date(this.atendimento.dataFim));
    }
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

    const defaultInicio = this.form.get('dataInicio')?.value ?? new Date();
    const defaultFim = this.form.get('dataFim')?.value ?? this.endOfDay(defaultInicio);
    this.procedimentos = (this.atendimento.procedimentos ?? []).map((p) => ({
      procedimentoId: p.procedimentoId,
      procedimentoCodigo: p.procedimentoCodigo,
      procedimentoDescricao: p.procedimentoDescricao,
      convenioId: p.convenioId,
      convenioNome: p.convenioNome,
      tabelaItemId: p.tabelaItemId,
      tabelaItemValor: p.tabelaItemValor,
      dataInicio: p.dataInicio ? new Date(p.dataInicio) : defaultInicio,
      dataFim: p.dataFim ? new Date(p.dataFim) : defaultFim,
    }));
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
      this.carregarCategoriasConvenio(this.atendimento.convenioId);
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
        // Busca o DTO completo para obter responsavelId/responsavelNome
        this.pessoaService.findById(result.entity.id!).subscribe((resp) => {
          const paciente = resp.body;
          if (paciente?.responsavelId && !this.responsavelSelecionado) {
            this.responsavelSelecionado = {
              id: paciente.responsavelId,
              nome: paciente.responsavelNome,
            } as PessoaDTO;
            this.form.get('responsavelId')?.setValue(paciente.responsavelId);
          }
        });
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
        this.carregarCategoriasConvenio(result.entity.id!);
      }
    });
  }

  private carregarCategoriasConvenio(convenioId: string): void {
    this.form.get('convenioCategoriaId')?.setValue('');
    this.categoriasOptions = [];
    this.convenioCategoriaService.listarPorConvenio(convenioId).subscribe((categorias) => {
      this.categoriasOptions = categorias;
      this.form.get('convenioCategoriaId')?.enable();
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
      title: $localize`Adicionar Procedimento`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.adicionarProcedimento(result.entity);
      }
    });
  }

  adicionarProcedimento(procedimento: ProcedimentoDTO): void {
    const dataInicio: Date = this.form.get('dataInicio')?.value ?? new Date();
    const dataFim: Date = this.form.get('dataFim')?.value ?? this.endOfDay(dataInicio);
    this.procedimentos = [...this.procedimentos, {
      procedimentoId: procedimento.id,
      procedimentoCodigo: procedimento.codigo,
      procedimentoDescricao: procedimento.descricao,
      convenioId: this.convenioSelecionado?.id,
      convenioNome: this.convenioSelecionado?.nome,
      dataInicio: new Date(dataInicio),
      dataFim: new Date(dataFim),
    }];
  }

  pesquisarConvenioProcedimento(index: number): void {
    const config: EntitySearchConfig<ConvenioDTO> = {
      service: this.convenioService,
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [{ key: 'nome', label: $localize`Nome` }],
      title: $localize`Selecionar Convênio do Procedimento`,
    };
    this.entitySearchService.search(config).subscribe((result) => {
      if (!result.cancelled && result.entity) {
        this.procedimentos = this.procedimentos.map((p, i) =>
          i === index ? { ...p, convenioId: result.entity!.id, convenioNome: result.entity!.nome } : p
        );
      }
    });
  }

  limparConvenioProcedimento(index: number): void {
    this.procedimentos = this.procedimentos.map((p, i) =>
      i === index ? { ...p, convenioId: undefined, convenioNome: undefined } : p
    );
  }

  removerProcedimento(index: number): void {
    this.procedimentos = this.procedimentos.filter((_, i) => i !== index);
  }

  formatarValor(valor: number | undefined): string {
    if (valor == null) return '—';
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  private endOfDay(date: Date): Date {
    const end = new Date(date);
    end.setHours(23, 59, 59, 0);
    return end;
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
    this.form.get('convenioCategoriaId')?.setValue('');
    this.form.get('convenioCategoriaId')?.disable();
    this.categoriasOptions = [];
  }
  limparProfissionalAtendimento(): void {
    this.profissionalAtendimentoSelecionado = null;
    this.form.get('profissionalAtendimentoId')?.setValue('');
  }
  limparProfissionalResponsavel(): void {
    this.profissionalResponsavelSelecionado = null;
    this.form.get('profissionalResponsavelId')?.setValue('');
  }

  // =========================================================================

  save() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return;
    }
    if (this.procedimentos.length === 0) {
      this.messages.erro($localize`Informe ao menos um procedimento.`);
      return;
    }

    const raw = this.form.getRawValue();
    const dataInicio: Date = raw.dataInicio;
    const dataFim: Date = raw.dataFim;

    this.atendimento.dataInicio = dataInicio ? dataInicio.toISOString() : undefined;
    this.atendimento.dataFim = dataFim ? dataFim.toISOString() : undefined;
    this.atendimento.setorId = raw.setorId || undefined;
    this.atendimento.pacienteId = raw.pacienteId || undefined;
    this.atendimento.responsavelId = raw.responsavelId || undefined;
    this.atendimento.convenioId = raw.convenioId || undefined;
    this.atendimento.convenioCategoriaId = raw.convenioCategoriaId || undefined;
    this.atendimento.profissionalAtendimentoId = raw.profissionalAtendimentoId || undefined;
    this.atendimento.profissionalResponsavelId = raw.profissionalResponsavelId || undefined;
    this.atendimento.procedimentos = this.procedimentos.map((p) => {
      const dto = new AtendimentoProcedimentoDTO();
      dto.procedimentoId = p.procedimentoId;
      dto.procedimentoCodigo = p.procedimentoCodigo;
      dto.procedimentoDescricao = p.procedimentoDescricao;
      dto.convenioId = p.convenioId;
      dto.convenioNome = p.convenioNome;
      dto.tabelaItemId = p.tabelaItemId;
      dto.dataInicio = p.dataInicio.toISOString();
      dto.dataFim = p.dataFim.toISOString();
      return dto;
    });
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

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  goBackFn = () => {
    this.closeDetail.emit();
  };
}
