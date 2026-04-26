import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { BaseComponent } from '../../../base/base.component';
import { AgendaService } from '../agenda/agenda.service';
import { AgendamentoService } from './agendamento.service';
import { AgendaGridDTO } from '../agenda/model/agenda-grid-dto';
import { SlotDTO } from './model/slot-dto';
import { PageRequest } from '../../../base/model/page-request';
import { AgendarComponent } from './agendar/agendar.component';
import { MessageService } from '../../../base/messages/messages.service';
import { ToolbarActionModel } from '../../../base/model/toolbar-action.model';

type ViewMode = 'CALENDAR' | 'FORM';

@Component({
  selector: 'gi-agendamento',
  standalone: true,
  imports: [BaseComponent, FormsModule, SelectModule, DatePickerModule, AgendarComponent],
  providers: [AgendaService, AgendamentoService],
  templateUrl: './agendamento.component.html',
  styleUrl: './agendamento.component.css',
})
export class AgendamentoComponent implements OnInit {
  viewMode: ViewMode = 'CALENDAR';
  titulo = $localize`Agendamentos`;
  toolbarActions: ToolbarActionModel[] = [];

  agendas: AgendaGridDTO[] = [];
  agendaSelecionada: AgendaGridDTO | null = null;
  dataSelecionada: Date = new Date();

  slots: SlotDTO[] = [];
  carregandoSlots = false;
  buscandoProximaData = false;
  slotDestacado: string | null = null;

  slotSelecionado: SlotDTO | null = null;
  agendamentoId: string | null = null;

  readonly agendaPlaceholder = $localize`Selecione uma agenda`;

  private agendaService = inject(AgendaService);
  private agendamentoService = inject(AgendamentoService);
  private messages = inject(MessageService);
  private router = inject(Router);

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

  carregarAgendas(): void {
    this.agendaService.listAll(PageRequest.empty()).subscribe((response) => {
      this.agendas = (response.body ?? []).filter((a) => !a.deleted);
    });
  }

  onAgendaChange(): void {
    this.slots = [];
    this.buscarSlots();
  }

  onDataChange(): void {
    this.buscarSlots();
  }

  buscarSlots(): void {
    if (!this.agendaSelecionada?.id || !this.dataSelecionada) {
      return;
    }
    this.carregandoSlots = true;
    this.slots = [];
    const dateStr = this.toDateStr(this.dataSelecionada);
    this.agendamentoService.listarSlots(this.agendaSelecionada.id, dateStr, dateStr).subscribe({
      next: (response) => {
        this.slots = response.body ?? [];
        this.carregandoSlots = false;
      },
      error: () => {
        this.carregandoSlots = false;
      },
    });
  }

  buscarProximaData(): void {
    if (!this.agendaSelecionada?.id) return;

    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const dataSel = new Date(this.dataSelecionada);
    dataSel.setHours(0, 0, 0, 0);
    const dataInicio = dataSel < hoje ? new Date(hoje) : new Date(dataSel);

    if (dataInicio.getTime() === dataSel.getTime() && this.slots.some((s) => s.livre)) {
      this.destacarPrimeiroLivre(this.slots);
      return;
    }

    this.buscandoProximaData = true;
    this.procurarSlotLivre(dataInicio, 0);
  }

  private procurarSlotLivre(date: Date, tentativa: number): void {
    if (tentativa > 90) {
      this.buscandoProximaData = false;
      this.messages.alerta($localize`Nenhuma data com horários livres encontrada nos próximos 90 dias.`);
      return;
    }
    const dateStr = this.toDateStr(date);
    this.agendamentoService.listarSlots(this.agendaSelecionada!.id!, dateStr, dateStr).subscribe({
      next: (response) => {
        const slots = response.body ?? [];
        if (slots.some((s) => s.livre)) {
          this.dataSelecionada = new Date(date);
          this.slots = slots;
          this.buscandoProximaData = false;
          this.destacarPrimeiroLivre(slots);
        } else {
          const proxima = new Date(date);
          proxima.setDate(proxima.getDate() + 1);
          this.procurarSlotLivre(proxima, tentativa + 1);
        }
      },
      error: () => { this.buscandoProximaData = false; },
    });
  }

  private destacarPrimeiroLivre(slots: SlotDTO[]): void {
    const primeiroLivre = slots.find((s) => s.livre);
    if (!primeiroLivre) return;
    this.slotDestacado = null;
    setTimeout(() => {
      this.slotDestacado = primeiroLivre.dataHoraInicio ?? null;
      setTimeout(() => { this.slotDestacado = null; }, 1600);
    });
  }

  onSlotClick(slot: SlotDTO): void {
    this.slotSelecionado = slot;
    this.agendamentoId = slot.livre ? null : (slot.agendamentoId ?? null);
    this.viewMode = 'FORM';
  }

  diaAnterior(): void {
    const d = new Date(this.dataSelecionada);
    d.setDate(d.getDate() - 1);
    this.dataSelecionada = d;
    this.buscarSlots();
  }

  proximoDia(): void {
    const d = new Date(this.dataSelecionada);
    d.setDate(d.getDate() + 1);
    this.dataSelecionada = d;
    this.buscarSlots();
  }

  voltarParaCalendario(): void {
    this.viewMode = 'CALENDAR';
    this.slotSelecionado = null;
    this.agendamentoId = null;
    this.buscarSlots();
  }

  get isDataPassada(): boolean {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const data = new Date(this.dataSelecionada);
    data.setHours(0, 0, 0, 0);
    return data < hoje;
  }

  get slotsLivres(): number {
    return this.slots.filter((s) => s.livre).length;
  }

  get slotsOcupados(): number {
    return this.slots.filter((s) => !s.livre).length;
  }

  formatarHora(isoString: string | undefined): string {
    if (!isoString) return '';
    return new Date(isoString).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  getSlotClass(slot: SlotDTO): string {
    if (this.isDataPassada) return 'slot slot--passado';
    if (slot.dataHoraInicio === this.slotDestacado) return 'slot slot--livre slot--destaque';
    return slot.livre ? 'slot slot--livre' : 'slot slot--ocupado';
  }

  getSlotDetalhe(slot: SlotDTO): string {
    const parts = [slot.convenioNome, slot.procedimentoNome].filter(Boolean);
    return parts.join(' · ');
  }

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
