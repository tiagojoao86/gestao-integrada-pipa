import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { ProcedimentoDetalheComponent } from './detalhe/procedimento-detalhe.component';
import { ProcedimentoGridComponent } from './grid/procedimento-grid.component';

@Component({
  selector: 'gi-procedimento',
  imports: [ProcedimentoDetalheComponent, ProcedimentoGridComponent],
  templateUrl: './procedimento.component.html',
  styleUrl: './procedimento.component.css',
})
export class ProcedimentoComponent {
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
