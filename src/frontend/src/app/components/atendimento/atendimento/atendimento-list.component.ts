import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { AtendimentoDetalheComponent } from './detalhe/atendimento-detalhe.component';
import { AtendimentoGridComponent } from './grid/atendimento-grid.component';

@Component({
  selector: 'gi-atendimento-list',
  imports: [AtendimentoDetalheComponent, AtendimentoGridComponent],
  templateUrl: './atendimento-list.component.html',
  styleUrl: './atendimento-list.component.css',
})
export class AtendimentoListComponent {
  viewMode: ViewMode = ViewMode.GRID;
  detailId: string | number = 'add';

  toggleView() {
    this.viewMode = this.viewMode === ViewMode.GRID ? ViewMode.DETAIL : ViewMode.GRID;
  }

  openDetail($event: string | number) {
    this.detailId = $event;
    this.toggleView();
  }
}
