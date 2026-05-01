import { Component, inject, OnInit } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { AtendimentoDetalheComponent } from './detalhe/atendimento-detalhe.component';
import { AtendimentoGridComponent } from './grid/atendimento-grid.component';
import { IniciarAtendimentoState } from './model/iniciar-atendimento-state';
import { AgendamentoService } from '../agendamento/agendamento/agendamento.service';

@Component({
  selector: 'gi-atendimento-list',
  imports: [AtendimentoDetalheComponent, AtendimentoGridComponent],
  providers: [AgendamentoService],
  templateUrl: './atendimento-list.component.html',
  styleUrl: './atendimento-list.component.css',
})
export class AtendimentoListComponent implements OnInit {
  private agendamentoService = inject(AgendamentoService);

  viewMode: ViewMode = ViewMode.GRID;
  detailId: string | number = 'add';
  preencherDeAgendamento: IniciarAtendimentoState | null = null;

  ngOnInit(): void {
    const state = history.state as {
      iniciarDe?: IniciarAtendimentoState;
      abrirAtendimentoId?: string;
    };
    if (state?.abrirAtendimentoId) {
      this.detailId = state.abrirAtendimentoId;
      this.viewMode = ViewMode.DETAIL;
    } else if (state?.iniciarDe) {
      this.preencherDeAgendamento = state.iniciarDe;
      this.detailId = 'add';
      this.viewMode = ViewMode.DETAIL;
    }
  }

  onAtendimentoSalvo(atendimentoId: string): void {
    const agendamentoId = this.preencherDeAgendamento?.agendamentoId;
    this.preencherDeAgendamento = null;
    if (agendamentoId) {
      this.agendamentoService.vincularAtendimento(agendamentoId, atendimentoId).subscribe();
    }
    this.viewMode = ViewMode.GRID;
  }

  toggleView() {
    this.preencherDeAgendamento = null;
    this.viewMode = this.viewMode === ViewMode.GRID ? ViewMode.DETAIL : ViewMode.GRID;
  }

  openDetail($event: string | number) {
    this.preencherDeAgendamento = null;
    this.detailId = $event;
    this.toggleView();
  }
}
