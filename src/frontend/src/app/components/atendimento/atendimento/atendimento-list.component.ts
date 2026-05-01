import { Component, OnInit } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { AtendimentoDetalheComponent } from './detalhe/atendimento-detalhe.component';
import { AtendimentoGridComponent } from './grid/atendimento-grid.component';
import { IniciarAtendimentoState } from './model/iniciar-atendimento-state';

@Component({
  selector: 'gi-atendimento-list',
  imports: [AtendimentoDetalheComponent, AtendimentoGridComponent],
  templateUrl: './atendimento-list.component.html',
  styleUrl: './atendimento-list.component.css',
})
export class AtendimentoListComponent implements OnInit {
  viewMode: ViewMode = ViewMode.GRID;
  detailId: string | number = 'add';
  preencherDeAgendamento: IniciarAtendimentoState | null = null;

  ngOnInit(): void {
    const state = history.state as { iniciarDe?: IniciarAtendimentoState };
    if (state?.iniciarDe) {
      this.preencherDeAgendamento = state.iniciarDe;
      this.detailId = 'add';
      this.viewMode = ViewMode.DETAIL;
    }
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
