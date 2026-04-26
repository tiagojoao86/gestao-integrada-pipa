import { Component } from '@angular/core';
import { ViewMode } from '../../../base/model/view-mode.enum';
import { AgendaGridComponent } from './grid/agenda-grid.component';
import { AgendaDetalheComponent } from './detalhe/agenda-detalhe.component';

@Component({
  selector: 'gi-agenda',
  imports: [AgendaGridComponent, AgendaDetalheComponent],
  templateUrl: './agenda.component.html',
  styleUrl: './agenda.component.css',
})
export class AgendaComponent {
  viewMode: ViewMode = ViewMode.GRID;
  detailId: string | number = 'add';

  toggleView(): void {
    this.viewMode = this.viewMode === ViewMode.GRID ? ViewMode.DETAIL : ViewMode.GRID;
  }

  openDetail(id: string | number): void {
    this.detailId = id;
    this.toggleView();
  }
}
