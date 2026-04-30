import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { BaseComponent } from '../../../../base/base.component';
import { IftaLabelModule } from 'primeng/iftalabel';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DatePickerModule } from 'primeng/datepicker';
import { MessageModule } from 'primeng/message';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { AgendamentoService } from '../agendamento.service';
import { AgendamentoDTO } from '../model/agendamento-dto';
import { SlotDTO } from '../model/slot-dto';
import { MessageService } from '../../../../base/messages/messages.service';
import { AuthService } from '../../../../base/auth/auth-service';
import { RouteConstants } from '../../../../base/constants/route-constants';
import { SystemModuleKey } from '../../../../base/enum/system-module-key.enum';
import { ToolbarActionModel } from '../../../../base/model/toolbar-action.model';
import { EntityFieldComponent } from '../../../../base/entity-field/entity-field.component';
import { EntitySearchService } from '../../../../base/entity-search/entity-search.service';
import { EntitySearchConfig } from '../../../../base/entity-search/entity-search.model';
import { AgendaDTO } from '../../agenda/model/agenda-dto';
import { AgendaService } from '../../agenda/agenda.service';
import { PessoaDTO } from '../../../../cadastro/pessoa/model/pessoa-dto';
import { PessoaService } from '../../../../cadastro/pessoa/pessoa.service';
import { PessoaDetalheComponent } from '../../../../cadastro/pessoa/pessoa-detalhe/pessoa-detalhe.component';
import { ConvenioDTO } from '../../../convenio/model/convenio-dto';
import { ConvenioService } from '../../../convenio/convenio.service';
import { ConvenioCategoriaService } from '../../../convenio-categoria/convenio-categoria.service';
import { ConvenioCategoriaGridDTO } from '../../../convenio-categoria/model/convenio-categoria-grid-dto';
import { ProcedimentoDTO } from '../../../procedimento/model/procedimento-dto';
import { ProcedimentoService } from '../../../procedimento/procedimento.service';

@Component({
  selector: 'gi-agendar',
  standalone: true,
  imports: [
    BaseComponent,
    ReactiveFormsModule,
    FormsModule,
    IftaLabelModule,
    InputTextModule,
    TextareaModule,
    DatePickerModule,
    MessageModule,
    ButtonModule,
    SelectModule,
    EntityFieldComponent,
    PessoaDetalheComponent,
  ],
  providers: [
    AgendamentoService,
    AgendaService,
    PessoaService,
    ConvenioService,
    ConvenioCategoriaService,
    ProcedimentoService,
  ],
  templateUrl: './agendar.component.html',
  styleUrl: './agendar.component.css',
})
export class AgendarComponent implements OnInit {
  @Input() detailId: string | number | null = null;
  @Input() agendaIdPre: string | null = null;
  @Input() agendaNomePre: string | null = null;
  @Input() agendaProfissionalNomePre: string | null = null;
  @Input() slotInicio: string | null = null;
  @Input() slotFim: string | null = null;
  @Input() somenteLeitura = false;
  @Output() closeDetail = new EventEmitter<void>();

  form!: FormGroup;
  dto: AgendamentoDTO = new AgendamentoDTO();
  titulo = $localize`Agendamento: `;
  toolbarActions: ToolbarActionModel[] = [];
  canEdit = false;
  showPessoaDetalhe = false;

  agendaSelecionada: AgendaDTO | null = null;
  pacienteSelecionado: PessoaDTO | null = null;
  convenioSelecionado: ConvenioDTO | null = null;
  categorias: ConvenioCategoriaGridDTO[] = [];
  procedimentoSelecionado: ProcedimentoDTO | null = null;

  dataInicio: Date | null = null;
  dataFim: Date | null = null;
  slots: SlotDTO[] = [];
  slotsSelecionados: SlotDTO[] = [];
  conflitos: AgendamentoDTO[] = [];
  carregandoSlots = false;

  readonly agendaLabel = $localize`Agenda`;
  readonly pacienteLabel = $localize`Paciente`;
  readonly convenioLabel = $localize`Convênio (opcional)`;
  readonly categoriaLabel = $localize`Categoria`;
  readonly procedimentoLabel = $localize`Procedimento (opcional)`;

  readonly agendaSearchConfig: EntitySearchConfig<AgendaDTO>;
  readonly pacienteSearchConfig: EntitySearchConfig<PessoaDTO>;
  readonly convenioSearchConfig: EntitySearchConfig<ConvenioDTO>;
  readonly procedimentoSearchConfig: EntitySearchConfig<ProcedimentoDTO>;

  private fb = inject(FormBuilder);
  private service = inject(AgendamentoService);
  private agendaService = inject(AgendaService);
  private pessoaService = inject(PessoaService);
  private convenioService = inject(ConvenioService);
  private convenioCategoriaService = inject(ConvenioCategoriaService);
  private procedimentoService = inject(ProcedimentoService);
  private messages = inject(MessageService);
  private auth = inject(AuthService);
  private entitySearchService = inject(EntitySearchService);

  constructor() {
    this.agendaSearchConfig = {
      service: this.agendaService,
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'profissionalNome', label: $localize`Profissional` },
      ],
    };

    this.pacienteSearchConfig = {
      service: this.pessoaService,
      searchFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'cpf',  label: $localize`CPF` },
      ],
      resultFields: [
        { key: 'nome',      label: $localize`Nome` },
        { key: 'documento', label: $localize`CPF/CNPJ` },
      ],
    };

    this.convenioSearchConfig = {
      service: this
        .convenioService as unknown as EntitySearchConfig<ConvenioDTO>['service'],
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [{ key: 'nome', label: $localize`Nome` }],
    };

    this.procedimentoSearchConfig = {
      service: this
        .procedimentoService as unknown as EntitySearchConfig<ProcedimentoDTO>['service'],
      searchFields: [{ key: 'descricao', label: $localize`Descrição` }],
      resultFields: [
        { key: 'codigo', label: $localize`Código` },
        { key: 'descricao', label: $localize`Descrição` },
      ],
    };
  }

  get calendarMode(): boolean {
    return this.slotInicio != null;
  }

  get agendaDisplayLabel(): string | null {
    if (!this.agendaSelecionada) return null;
    const prof = this.agendaSelecionada.profissionalNome;
    return prof
      ? `${this.agendaSelecionada.nome} (${prof})`
      : (this.agendaSelecionada.nome ?? null);
  }

  ngOnInit(): void {
    this.initForm();
    this.checkPermissions();
    this.createToolbarActions();

    if (this.detailId === RouteConstants.P_ADD) {
      if (this.calendarMode) {
        const nomeTitulo = this.agendaNomePre ?? $localize`Novo`;
        const profTitulo = this.agendaProfissionalNomePre
          ? ` (${this.agendaProfissionalNomePre})`
          : '';
        this.titulo += nomeTitulo + profTitulo;
        this.preencherDoCalendario();
      } else {
        this.titulo += $localize`Novo`;
      }
    } else if (this.detailId) {
      this.prepareForEdit();
    }
  }

  private preencherDoCalendario(): void {
    this.agendaSelecionada = {
      id: this.agendaIdPre!,
      nome: this.agendaNomePre ?? '',
      profissionalNome: this.agendaProfissionalNomePre ?? undefined,
    } as AgendaDTO;
    this.form.get('agendaId')?.setValue(this.agendaIdPre);
    const slot = new SlotDTO();
    slot.dataHoraInicio = this.slotInicio!;
    slot.dataHoraFim = this.slotFim ?? undefined;
    slot.livre = true;
    this.slotsSelecionados = [slot];
  }

  formatarSlotHeader(): string {
    if (!this.slotInicio) return '';
    const inicio = new Date(this.slotInicio);
    const data = inicio.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
    const horaInicio = inicio.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    });
    if (!this.slotFim) return `${data} ${horaInicio}`;
    const horaFim = new Date(this.slotFim).toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    });
    return `${data} ${horaInicio} – ${horaFim}`;
  }

  private initForm(): void {
    this.form = this.fb.group({
      agendaId: ['', Validators.required],
      pacienteId: ['', Validators.required],
      convenioId: [null as string | null],
      categoriaId: [{ value: null as string | null, disabled: true }],
      procedimentoId: [null as string | null],
      observacao: ['', Validators.maxLength(1000)],
    });
  }

  private checkPermissions(): void {
    this.canEdit = this.auth.hasAuthorityEditarToModulo(
      SystemModuleKey.AGENDAMENTO_AGENDAMENTO,
    );
  }

  private createToolbarActions(): void {
    this.toolbarActions = [
      {
        action: () => this.goBackFn(),
        icon: 'close',
        title: $localize`Cancelar (esc)`,
        shortcut: 'escape',
      },
    ];
    if (this.canEdit && !this.somenteLeitura) {
      this.toolbarActions.push({
        action: () => this.salvar(),
        icon: 'save',
        title: $localize`Salvar (enter)`,
        shortcut: 'enter',
      });
    }
    if (this.canEdit && this.detailId === RouteConstants.P_ADD) {
      this.toolbarActions.push({
        action: () => this.abrirCadastroPaciente(),
        icon: 'person_add',
        title: $localize`Cadastrar novo paciente`,
      });
    }
  }

  private prepareForEdit(): void {
    this.service.findById(String(this.detailId!)).subscribe((response) => {
      this.dto = response.body!;
      this.titulo += this.dto.pacienteNome;
      this.fillForm();
    });
  }

  private fillForm(): void {
    this.form.patchValue({
      agendaId: this.dto.agendaId ?? '',
      pacienteId: this.dto.pacienteId ?? '',
      convenioId: this.dto.convenioId ?? null,
      categoriaId: this.dto.categoriaId ?? null,
      procedimentoId: this.dto.procedimentoId ?? null,
      observacao: this.dto.observacao ?? '',
    });

    if (this.dto.agendaId) {
      this.agendaSelecionada = {
        id: this.dto.agendaId,
        nome: this.dto.agendaNome,
      } as AgendaDTO;
    }
    if (this.dto.pacienteId) {
      this.pacienteSelecionado = {
        id: this.dto.pacienteId,
        nome: this.dto.pacienteNome,
      } as PessoaDTO;
    }
    if (this.dto.convenioId) {
      this.convenioSelecionado = {
        id: this.dto.convenioId,
        nome: this.dto.convenioNome,
      } as ConvenioDTO;
      this.carregarCategorias(this.dto.convenioId, this.dto.categoriaId);
    }
    if (this.dto.procedimentoId) {
      this.procedimentoSelecionado = {
        id: this.dto.procedimentoId,
        descricao: this.dto.procedimentoNome,
      } as unknown as ProcedimentoDTO;
    }

    if (this.dto.horariosInicio?.length) {
      if (this.calendarMode) {
        this.slotsSelecionados = this.dto.horariosInicio.map((inicio, i) => {
          const slot = new SlotDTO();
          slot.dataHoraInicio = inicio;
          slot.dataHoraFim = this.dto.horariosFim?.[i];
          slot.livre = false;
          return slot;
        });
      } else {
        const primeiro = new Date(this.dto.horariosInicio[0]);
        const ultimo = new Date(
          this.dto.horariosInicio[this.dto.horariosInicio.length - 1],
        );
        this.dataInicio = new Date(
          primeiro.getFullYear(),
          primeiro.getMonth(),
          primeiro.getDate(),
        );
        this.dataFim = new Date(
          ultimo.getFullYear(),
          ultimo.getMonth(),
          ultimo.getDate(),
        );
        this.buscarSlots();
      }
    }
  }

  pesquisarAgenda(): void {
    this.entitySearchService
      .search(this.agendaSearchConfig)
      .subscribe((result) => {
        if (!result.cancelled && result.entity) {
          this.onAgendaSelected(result.entity);
        }
      });
  }

  onAgendaSelected(entity: unknown): void {
    const agenda = entity as AgendaDTO;
    this.agendaSelecionada = agenda;
    this.form.get('agendaId')?.setValue(agenda.id ?? '');
    this.slots = [];
    this.slotsSelecionados = [];
  }

  limparAgenda(): void {
    this.agendaSelecionada = null;
    this.form.get('agendaId')?.setValue('');
    this.slots = [];
    this.slotsSelecionados = [];
  }

  pesquisarPaciente(): void {
    this.entitySearchService
      .search(this.pacienteSearchConfig)
      .subscribe((result) => {
        if (!result.cancelled && result.entity) {
          this.onPacienteSelected(result.entity);
        }
      });
  }

  onPacienteSelected(entity: unknown): void {
    const pessoa = entity as PessoaDTO;
    this.pacienteSelecionado = pessoa;
    this.form.get('pacienteId')?.setValue(pessoa.id ?? '');
    this.verificarConflitos();
  }

  limparPaciente(): void {
    this.pacienteSelecionado = null;
    this.form.get('pacienteId')?.setValue('');
    this.conflitos = [];
  }

  pesquisarConvenio(): void {
    this.entitySearchService
      .search(this.convenioSearchConfig)
      .subscribe((result) => {
        if (!result.cancelled && result.entity) {
          this.onConvenioSelected(result.entity);
        }
      });
  }

  onConvenioSelected(entity: unknown): void {
    const convenio = entity as ConvenioDTO;
    this.convenioSelecionado = convenio;
    this.form.get('convenioId')?.setValue(convenio.id ?? null);
    this.form.get('categoriaId')?.setValue(null);
    this.carregarCategorias(convenio.id!);
  }

  limparConvenio(): void {
    this.convenioSelecionado = null;
    this.form.get('convenioId')?.setValue(null);
    this.form.get('categoriaId')?.setValue(null);
    this.form.get('categoriaId')?.disable();
    this.categorias = [];
  }

  private carregarCategorias(convenioId: string, categoriaIdPreSelecionada?: string): void {
    this.convenioCategoriaService.listarPorConvenio(convenioId).subscribe({
      next: (cats) => {
        this.categorias = cats.filter((c) => !c.deleted);
        if (this.categorias.length > 0) {
          this.form.get('categoriaId')?.enable();
        }
        if (categoriaIdPreSelecionada) {
          this.form.get('categoriaId')?.setValue(categoriaIdPreSelecionada);
        }
      },
      error: () => {
        this.categorias = [];
      },
    });
  }

  pesquisarProcedimento(): void {
    this.entitySearchService
      .search(this.procedimentoSearchConfig)
      .subscribe((result) => {
        if (!result.cancelled && result.entity) {
          this.onProcedimentoSelected(result.entity);
        }
      });
  }

  onProcedimentoSelected(entity: unknown): void {
    const proc = entity as ProcedimentoDTO;
    this.procedimentoSelecionado = proc;
    this.form.get('procedimentoId')?.setValue(proc.id ?? '');
  }

  limparProcedimento(): void {
    this.procedimentoSelecionado = null;
    this.form.get('procedimentoId')?.setValue('');
  }

  buscarSlots(): void {
    const agendaId = this.form.get('agendaId')?.value;
    if (!agendaId || !this.dataInicio || !this.dataFim) {
      this.messages.alerta(
        $localize`Selecione uma agenda e o período para buscar os slots.`,
      );
      return;
    }
    this.carregandoSlots = true;
    this.slotsSelecionados = [];
    const inicio = this.toDateStr(this.dataInicio);
    const fim = this.toDateStr(this.dataFim);
    this.service.listarSlots(agendaId, inicio, fim).subscribe({
      next: (response) => {
        this.slots = response.body ?? [];
        this.carregandoSlots = false;
        if (
          this.detailId !== RouteConstants.P_ADD &&
          this.dto.horariosInicio?.length
        ) {
          this.preSelectSlots();
        }
      },
      error: () => {
        this.carregandoSlots = false;
      },
    });
  }

  private preSelectSlots(): void {
    const iniciosSalvos = new Set(this.dto.horariosInicio ?? []);
    this.slotsSelecionados = this.slots.filter(
      (s) => s.dataHoraInicio && iniciosSalvos.has(s.dataHoraInicio),
    );
  }

  toggleSlot(slot: SlotDTO): void {
    if (!slot.livre && !this.isSlotSelecionado(slot)) return;
    const idx = this.slotsSelecionados.findIndex(
      (s) => s.dataHoraInicio === slot.dataHoraInicio,
    );
    if (idx >= 0) {
      this.slotsSelecionados.splice(idx, 1);
    } else {
      this.slotsSelecionados.push(slot);
    }
    this.verificarConflitos();
  }

  isSlotSelecionado(slot: SlotDTO): boolean {
    return this.slotsSelecionados.some(
      (s) => s.dataHoraInicio === slot.dataHoraInicio,
    );
  }

  private verificarConflitos(): void {
    const pacienteId = this.form.get('pacienteId')?.value;
    if (!pacienteId || this.slotsSelecionados.length === 0) {
      this.conflitos = [];
      return;
    }
    const datas = this.slotsSelecionados
      .map((s) => s.dataHoraInicio?.split('T')[0])
      .filter(Boolean) as string[];
    if (datas.length === 0) return;
    const minData = datas.reduce((a, b) => (a < b ? a : b));
    const maxData = datas.reduce((a, b) => (a > b ? a : b));
    this.service
      .conflitoPaciente(pacienteId, minData, maxData)
      .subscribe((response) => {
        this.conflitos = (response.body ?? []).filter(
          (ag) => ag.id !== this.detailId,
        );
      });
  }

  formatarSlot(slot: SlotDTO): string {
    if (!slot.dataHoraInicio) return '';
    const dt = new Date(slot.dataHoraInicio);
    const data = dt.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
    });
    const hora = dt.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    });
    return `${data} ${hora}`;
  }

  getSlotClass(slot: SlotDTO): string {
    if (this.isSlotSelecionado(slot)) return 'slot slot--selecionado';
    if (slot.livre) return 'slot slot--livre';
    return 'slot slot--ocupado';
  }

  salvar(): void {
    if (this.somenteLeitura) return;
    if (!this.validateBeforeSave()) return;
    this.populateDTOBeforeSend();
    this.service.save(this.dto, {
      onSuccess: () => {
        this.messages.sucesso($localize`Agendamento salvo com sucesso.`);
        this.goBackFn();
      },
    });
  }

  private validateBeforeSave(): boolean {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.messages.erro($localize`Existem campos inválidos.`);
      return false;
    }
    if (this.slotsSelecionados.length === 0) {
      this.messages.erro($localize`Selecione ao menos um horário.`);
      return false;
    }
    return true;
  }

  private populateDTOBeforeSend(): void {
    const raw = this.form.getRawValue();
    this.dto.agendaId = raw.agendaId || undefined;
    this.dto.pacienteId = raw.pacienteId || undefined;
    this.dto.convenioId = raw.convenioId || undefined;
    this.dto.categoriaId = raw.categoriaId || undefined;
    this.dto.procedimentoId = raw.procedimentoId || undefined;
    this.dto.observacao = raw.observacao || undefined;

    const sorted = [...this.slotsSelecionados].sort((a, b) =>
      (a.dataHoraInicio ?? '').localeCompare(b.dataHoraInicio ?? ''),
    );
    this.dto.horariosInicio = sorted.map((s) => s.dataHoraInicio!);
    this.dto.horariosFim = sorted.map((s) => s.dataHoraFim!);
  }

  isInvalid(campo: string): boolean {
    const fc: AbstractControl | null = this.form.get(campo);
    return fc !== null && fc.invalid && (fc.touched || fc.dirty);
  }

  private toDateStr(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  abrirCadastroPaciente(): void {
    this.showPessoaDetalhe = true;
  }

  fecharPessoaDetalhe(): void {
    this.showPessoaDetalhe = false;
  }

  onPacienteSalvo(pessoa: { id: string; nome: string }): void {
    this.pacienteSelecionado = {
      id: pessoa.id,
      nome: pessoa.nome,
    } as PessoaDTO;
    this.form.get('pacienteId')?.setValue(pessoa.id);
    this.showPessoaDetalhe = false;
    this.verificarConflitos();
  }

  goBackFn = (): void => {
    this.closeDetail.emit();
  };
}
