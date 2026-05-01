import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { IftaLabelModule } from 'primeng/iftalabel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BaseComponent } from '../../../base/base.component';
import { AgendaService } from '../agenda/agenda.service';
import { AgendamentoService } from './agendamento.service';
import { AgendaGridDTO } from '../agenda/model/agenda-grid-dto';
import { SlotDTO } from './model/slot-dto';
import { AgendamentoGridDTO } from './model/agendamento-grid-dto';
import { PageRequest } from '../../../base/model/page-request';
import { AgendarComponent } from './agendar/agendar.component';
import { MessageService } from '../../../base/messages/messages.service';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';
import { EntityFieldComponent } from '../../../base/entity-field/entity-field.component';
import { EntitySearchService } from '../../../base/entity-search/entity-search.service';
import { EntitySearchConfig } from '../../../base/entity-search/entity-search.model';
import { PessoaDTO } from '../../../cadastro/pessoa/model/pessoa-dto';
import { PessoaService } from '../../../cadastro/pessoa/pessoa.service';
import { DialogService } from '../../../base/dialog/dialog.service';
import { DialogResult } from '../../../base/dialog/dialog.model';
import { ButtonModule } from 'primeng/button';

type ViewMode = 'CALENDAR' | 'FORM';
type VisaoMode = 'PROFISSIONAL' | 'PACIENTE';
type PeriodoMode = '1D' | '7D' | '30D';

interface Coluna7D {
  data: Date;
  dataStr: string;
  label: string;
  isHoje: boolean;
  slots: SlotDTO[];
}

interface Celula30D {
  data: Date | null;
  dia: number;
  livres: number;
  ocupados: number;
}

interface GrupoAgendamento {
  dataStr: string;
  dataFormatada: string;
  diaSemana: string;
  agendamentos: AgendamentoGridDTO[];
}

@Component({
  selector: 'gi-agendamento',
  standalone: true,
  imports: [
    BaseComponent,
    FormsModule,
    SelectModule,
    DatePickerModule,
    IftaLabelModule,
    ProgressSpinnerModule,
    AgendarComponent,
    EntityFieldComponent,
    ButtonModule,
  ],
  providers: [AgendaService, AgendamentoService, PessoaService],
  templateUrl: './agendamento.component.html',
  styleUrl: './agendamento.component.css',
})
export class AgendamentoComponent implements OnInit {
  viewMode: ViewMode = 'CALENDAR';
  visaoMode: VisaoMode = 'PROFISSIONAL';
  periodoMode: PeriodoMode = '1D';
  titulo = $localize`Agendamentos`;
  toolbarActions: ToolbarActionModel[] = [];

  // profissional
  agendas: AgendaGridDTO[] = [];
  agendaSelecionada: AgendaGridDTO | null = null;
  dataSelecionada: Date = new Date();
  slots: SlotDTO[] = [];
  carregandoSlots = false;
  colunas7d: Coluna7D[] = [];
  celulas30d: Celula30D[] = [];
  carregandoMultiplos = false;
  buscandoProximaData = false;
  slotDestacado: string | null = null;
  slotSelecionado: SlotDTO | null = null;
  agendamentoId: string | null = null;

  // paciente
  pacienteSelecionado: PessoaDTO | null = null;
  dataInicioPaciente: Date = new Date();
  dataFimPaciente: Date = new Date();
  agendamentosPacienteAgrupados: GrupoAgendamento[] = [];
  carregandoPaciente = false;

  readonly agendaPlaceholder = $localize`Selecione uma agenda`;
  readonly pacienteLabel = $localize`Paciente`;
  readonly diasSemanaAbrev = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
  readonly diasSemanaCompleto = [
    'Domingo',
    'Segunda-feira',
    'Terça-feira',
    'Quarta-feira',
    'Quinta-feira',
    'Sexta-feira',
    'Sábado',
  ];

  readonly pacienteSearchConfig: EntitySearchConfig<PessoaDTO>;

  private agendaService = inject(AgendaService);
  private agendamentoService = inject(AgendamentoService);
  private pessoaService = inject(PessoaService);
  private messages = inject(MessageService);
  private entitySearchService = inject(EntitySearchService);
  private dialog = inject(DialogService);
  private router = inject(Router);

  constructor() {
    this.pacienteSearchConfig = {
      service: this.pessoaService,
      searchFields: [{ key: 'nome', label: $localize`Nome` }],
      resultFields: [
        { key: 'nome', label: $localize`Nome` },
        { key: 'documento', label: $localize`Documento` },
      ],
    };
  }

  ngOnInit(): void {
    this.createToolbarActions();
    this.carregarAgendas();
  }

  private createToolbarActions(): void {
    this.toolbarActions = [
      {
        action: () => this.buscarProximaData(),
        icon: 'event_available',
        title: $localize`Próxima data disponível`,
      },
    ];
  }

  // ── Visão / Período ────────────────────────────────────────────────────────

  onVisaoChange(modo: VisaoMode): void {
    this.visaoMode = modo;
    this.slotSelecionado = null;
  }

  onPeriodoChange(modo: PeriodoMode): void {
    this.periodoMode = modo;
    this.carregarPorPeriodo();
  }

  private carregarPorPeriodo(): void {
    if (!this.agendaSelecionada?.id) return;
    if (this.periodoMode === '1D') this.buscarSlots();
    else if (this.periodoMode === '7D') this.carregarSlots7D();
    else this.carregarSlots30D();
  }

  // ── Profissional — agenda / data ───────────────────────────────────────────

  carregarAgendas(): void {
    this.agendaService.listAll(PageRequest.empty()).subscribe((response) => {
      this.agendas = (response.body ?? []).filter((a) => !a.deleted);
    });
  }

  onAgendaChange(): void {
    this.slots = [];
    this.colunas7d = [];
    this.celulas30d = [];
    this.carregarPorPeriodo();
  }

  onDataChange(): void {
    this.carregarPorPeriodo();
  }

  diaAnterior(): void {
    const d = new Date(this.dataSelecionada);
    d.setDate(d.getDate() - 1);
    this.dataSelecionada = d;
    this.carregarPorPeriodo();
  }

  proximoDia(): void {
    const d = new Date(this.dataSelecionada);
    d.setDate(d.getDate() + 1);
    this.dataSelecionada = d;
    this.carregarPorPeriodo();
  }

  // ── Slots 1D ───────────────────────────────────────────────────────────────

  buscarSlots(): void {
    if (!this.agendaSelecionada?.id || !this.dataSelecionada) return;
    this.carregandoSlots = true;
    this.slots = [];
    const dateStr = this.toDateStr(this.dataSelecionada);
    this.agendamentoService
      .listarSlots(this.agendaSelecionada.id, dateStr, dateStr)
      .subscribe({
        next: (response) => {
          this.slots = response.body ?? [];
          this.carregandoSlots = false;
        },
        error: () => {
          this.carregandoSlots = false;
        },
      });
  }

  // ── Slots 7D ───────────────────────────────────────────────────────────────

  carregarSlots7D(): void {
    if (!this.agendaSelecionada?.id) return;
    this.carregandoMultiplos = true;
    this.colunas7d = [];
    const inicio = this.toDateStr(this.dataSelecionada);
    const fimDate = new Date(this.dataSelecionada);
    fimDate.setDate(fimDate.getDate() + 6);
    const fim = this.toDateStr(fimDate);
    this.agendamentoService
      .listarSlots(this.agendaSelecionada.id, inicio, fim)
      .subscribe({
        next: (response) => {
          this.buildColunas7D(response.body ?? []);
          this.carregandoMultiplos = false;
        },
        error: () => {
          this.carregandoMultiplos = false;
        },
      });
  }

  private buildColunas7D(slots: SlotDTO[]): void {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const slotsPorData = new Map<string, SlotDTO[]>();
    for (const slot of slots) {
      if (!slot.dataHoraInicio) continue;
      const dataStr = slot.dataHoraInicio.split('T')[0];
      const lista = slotsPorData.get(dataStr) ?? [];
      lista.push(slot);
      slotsPorData.set(dataStr, lista);
    }
    const colunas: Coluna7D[] = [];
    const current = new Date(this.dataSelecionada);
    current.setHours(0, 0, 0, 0);
    for (let i = 0; i < 7; i++) {
      const dataStr = this.toDateStr(current);
      const dia = String(current.getDate()).padStart(2, '0');
      const mes = String(current.getMonth() + 1).padStart(2, '0');
      colunas.push({
        data: new Date(current),
        dataStr,
        label: `${this.diasSemanaAbrev[current.getDay()]} ${dia}/${mes}`,
        isHoje: current.getTime() === hoje.getTime(),
        slots: slotsPorData.get(dataStr) ?? [],
      });
      current.setDate(current.getDate() + 1);
    }
    this.colunas7d = colunas;
  }

  get slotsLivres7D(): number {
    return this.colunas7d.reduce(
      (acc, c) => acc + c.slots.filter((s) => s.livre).length,
      0,
    );
  }

  get slotsOcupados7D(): number {
    return this.colunas7d.reduce(
      (acc, c) => acc + c.slots.filter((s) => !s.livre).length,
      0,
    );
  }

  // ── Slots 30D ──────────────────────────────────────────────────────────────

  carregarSlots30D(): void {
    if (!this.agendaSelecionada?.id) return;
    this.carregandoMultiplos = true;
    this.celulas30d = [];
    const inicio = this.toDateStr(this.dataSelecionada);
    const fimDate = new Date(this.dataSelecionada);
    fimDate.setDate(fimDate.getDate() + 29);
    const fim = this.toDateStr(fimDate);
    this.agendamentoService
      .listarSlots(this.agendaSelecionada.id, inicio, fim)
      .subscribe({
        next: (response) => {
          this.buildCelulas30D(response.body ?? []);
          this.carregandoMultiplos = false;
        },
        error: () => {
          this.carregandoMultiplos = false;
        },
      });
  }

  private buildCelulas30D(slots: SlotDTO[]): void {
    const totaisPorData = new Map<
      string,
      { livres: number; ocupados: number }
    >();
    for (const slot of slots) {
      if (!slot.dataHoraInicio) continue;
      const dataStr = slot.dataHoraInicio.split('T')[0];
      const atual = totaisPorData.get(dataStr) ?? { livres: 0, ocupados: 0 };
      if (slot.livre) atual.livres++;
      else atual.ocupados++;
      totaisPorData.set(dataStr, atual);
    }
    const cells: Celula30D[] = [];
    const startDate = new Date(this.dataSelecionada);
    startDate.setHours(0, 0, 0, 0);
    for (let i = 0; i < startDate.getDay(); i++) {
      cells.push({ data: null, dia: 0, livres: 0, ocupados: 0 });
    }
    const current = new Date(startDate);
    for (let i = 0; i < 30; i++) {
      const dataStr = this.toDateStr(current);
      const totais = totaisPorData.get(dataStr) ?? { livres: 0, ocupados: 0 };
      cells.push({
        data: new Date(current),
        dia: current.getDate(),
        ...totais,
      });
      current.setDate(current.getDate() + 1);
    }
    const lastDow = new Date(current.getTime() - 1).getDay();
    for (let i = lastDow + 1; i <= 6; i++) {
      cells.push({ data: null, dia: 0, livres: 0, ocupados: 0 });
    }
    this.celulas30d = cells;
  }

  onDiaClick30D(celula: Celula30D): void {
    if (!celula.data) return;
    this.dataSelecionada = new Date(celula.data);
    this.periodoMode = '1D';
    this.buscarSlots();
  }

  // ── Slots — interação ──────────────────────────────────────────────────────

  onSlotClick(slot: SlotDTO): void {
    this.slotSelecionado = slot;
    this.agendamentoId = slot.livre ? null : (slot.agendamentoId ?? null);
    this.viewMode = 'FORM';
  }

  onSlotClick7D(slot: SlotDTO, col: Coluna7D): void {
    this.dataSelecionada = new Date(col.data);
    this.onSlotClick(slot);
  }

  iniciarAtendimentoFromSlot(event: Event, slot: SlotDTO): void {
    event.stopPropagation();
    if (!slot.agendamentoId) return;
    if (slot.atendimentoId) {
      this.router.navigate(['/atendimento/atendimento'], {
        state: { abrirAtendimentoId: slot.atendimentoId },
      });
      return;
    }
    this.agendamentoService.findById(slot.agendamentoId).subscribe((response) => {
      const ag = response.body!;
      this.router.navigate(['/atendimento/atendimento'], {
        state: {
          iniciarDe: {
            agendamentoId: ag.id,
            pacienteId: ag.pacienteId,
            pacienteNome: ag.pacienteNome,
            profissionalId: ag.profissionalId,
            profissionalNome: ag.profissionalNome,
            convenioId: ag.convenioId,
            convenioNome: ag.convenioNome,
            convenioCategoriaId: ag.categoriaId,
            convenioCategoriaNome: ag.categoriaNome,
            procedimentoId: ag.procedimentoId,
            procedimentoNome: ag.procedimentoNome,
          },
        },
      });
    });
  }

  voltarParaCalendario(): void {
    this.viewMode = 'CALENDAR';
    this.slotSelecionado = null;
    this.agendamentoId = null;
    this.carregarPorPeriodo();
  }

  // ── Próxima data disponível ────────────────────────────────────────────────

  buscarProximaData(): void {
    if (!this.agendaSelecionada?.id || this.visaoMode !== 'PROFISSIONAL')
      return;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const dataSel = new Date(this.dataSelecionada);
    dataSel.setHours(0, 0, 0, 0);
    const dataInicio = dataSel < hoje ? new Date(hoje) : new Date(dataSel);
    if (
      dataInicio.getTime() === dataSel.getTime() &&
      this.slots.some((s) => s.livre)
    ) {
      this.destacarPrimeiroLivre(this.slots);
      return;
    }
    this.buscandoProximaData = true;
    this.procurarSlotLivre(dataInicio, 0);
  }

  private procurarSlotLivre(date: Date, tentativa: number): void {
    if (tentativa > 90) {
      this.buscandoProximaData = false;
      this.messages.alerta(
        $localize`Nenhuma data com horários livres encontrada nos próximos 90 dias.`,
      );
      return;
    }
    const dateStr = this.toDateStr(date);
    this.agendamentoService
      .listarSlots(this.agendaSelecionada!.id!, dateStr, dateStr)
      .subscribe({
        next: (response) => {
          const slots = response.body ?? [];
          if (slots.some((s) => s.livre)) {
            this.dataSelecionada = new Date(date);
            this.slots = slots;
            this.periodoMode = '1D';
            this.buscandoProximaData = false;
            this.destacarPrimeiroLivre(slots);
          } else {
            const proxima = new Date(date);
            proxima.setDate(proxima.getDate() + 1);
            this.procurarSlotLivre(proxima, tentativa + 1);
          }
        },
        error: () => {
          this.buscandoProximaData = false;
        },
      });
  }

  private destacarPrimeiroLivre(slots: SlotDTO[]): void {
    const primeiroLivre = slots.find((s) => s.livre);
    if (!primeiroLivre) return;
    this.slotDestacado = null;
    setTimeout(() => {
      this.slotDestacado = primeiroLivre.dataHoraInicio ?? null;
      setTimeout(() => {
        this.slotDestacado = null;
      }, 1600);
    });
  }

  // ── Visão paciente ─────────────────────────────────────────────────────────

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
    this.pacienteSelecionado = entity as PessoaDTO;
    this.carregarAgendamentosPaciente();
  }

  limparPaciente(): void {
    this.pacienteSelecionado = null;
    this.agendamentosPacienteAgrupados = [];
  }

  carregarAgendamentosPaciente(): void {
    if (!this.pacienteSelecionado?.id) return;
    this.carregandoPaciente = true;
    this.agendamentosPacienteAgrupados = [];
    const inicio = this.toDateStr(this.dataInicioPaciente);
    const fim = this.toDateStr(this.dataFimPaciente);
    this.agendamentoService
      .listarPorPaciente(this.pacienteSelecionado.id, inicio, fim)
      .subscribe({
        next: (response) => {
          this.buildGruposAgendamento(response.body ?? []);
          this.carregandoPaciente = false;
        },
        error: () => {
          this.carregandoPaciente = false;
        },
      });
  }

  private buildGruposAgendamento(agendamentos: AgendamentoGridDTO[]): void {
    const gruposMap = new Map<string, AgendamentoGridDTO[]>();
    for (const ag of agendamentos) {
      const dataStr = (ag.primeiraDataHora ?? ag.primeiraData ?? '').split(
        'T',
      )[0];
      if (!dataStr) continue;
      const lista = gruposMap.get(dataStr) ?? [];
      lista.push(ag);
      gruposMap.set(dataStr, lista);
    }
    this.agendamentosPacienteAgrupados = Array.from(gruposMap.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([dataStr, ags]) => {
        const data = new Date(dataStr + 'T00:00:00');
        return {
          dataStr,
          dataFormatada: data.toLocaleDateString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
          }),
          diaSemana: this.diasSemanaCompleto[data.getDay()],
          agendamentos: ags,
        };
      });
  }

  cancelarAgendamento(ag: AgendamentoGridDTO): void {
    this.dialog
      .showYesNo(
        $localize`Cancelar agendamento`,
        $localize`Deseja cancelar o agendamento de ${ag.pacienteNome}?`,
      )
      .subscribe((result) => {
        if (result !== DialogResult.YES) return;
        this.agendamentoService.cancelar(ag.id!).subscribe({
          next: () => {
            this.messages.sucesso($localize`Agendamento cancelado.`);
            this.carregarAgendamentosPaciente();
          },
        });
      });
  }

  // ── Auxiliares de estilo ───────────────────────────────────────────────────

  get isDataPassada(): boolean {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const data = new Date(this.dataSelecionada);
    data.setHours(0, 0, 0, 0);
    return data < hoje;
  }

  isDataPassadaStr(dataStr: string): boolean {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    return new Date(dataStr + 'T00:00:00') < hoje;
  }

  getSlotClass(slot: SlotDTO): string {
    if (this.isDataPassada) return 'slot slot--passado';
    if (slot.dataHoraInicio === this.slotDestacado)
      return 'slot slot--livre slot--destaque';
    return slot.livre ? 'slot slot--livre' : 'slot slot--ocupado';
  }

  getSlotClass7D(slot: SlotDTO, dataStr: string): string {
    if (this.isDataPassadaStr(dataStr))
      return 'slot slot--compact slot--passado';
    if (slot.dataHoraInicio === this.slotDestacado)
      return 'slot slot--compact slot--livre slot--destaque';
    return slot.livre
      ? 'slot slot--compact slot--livre'
      : 'slot slot--compact slot--ocupado';
  }

  getCelula30dClass(celula: Celula30D): string {
    if (!celula.data) return 'celula-30d celula-vazia';
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const data = new Date(celula.data);
    data.setHours(0, 0, 0, 0);
    if (data.getTime() === hoje.getTime()) return 'celula-30d celula-hoje';
    if (data < hoje) return 'celula-30d celula-passada';
    return celula.livres > 0 || celula.ocupados > 0
      ? 'celula-30d celula-com-slots'
      : 'celula-30d celula-sem-slots';
  }

  getStatusClass(status: string | undefined): string {
    return `paciente-card-status status--${(status ?? 'agendado').toLowerCase()}`;
  }

  // ── Getters 1D ─────────────────────────────────────────────────────────────

  get slotsLivres(): number {
    return this.slots.filter((s) => s.livre).length;
  }
  get slotsOcupados(): number {
    return this.slots.filter((s) => !s.livre).length;
  }

  // ── Formatação ─────────────────────────────────────────────────────────────

  formatarHora(isoString: string | undefined): string {
    if (!isoString) return '';
    return new Date(isoString).toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  getSlotDetalhe(slot: SlotDTO): string {
    return [slot.convenioNome, slot.procedimentoNome]
      .filter(Boolean)
      .join(' · ');
  }

  // ── Misc ───────────────────────────────────────────────────────────────────

  private toDateStr(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  goBackFn = (): void => {
    this.router.navigate(['/atendimento']);
  };
}
