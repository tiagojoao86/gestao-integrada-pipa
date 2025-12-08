import { Component } from '@angular/core';

import { ViewMode } from '../../base/model/view-mode.enum';
import { TituloDetalheComponent } from './titulo-detalhe/titulo-detalhe.component';
import { TituloGridComponent } from './titulo-grid/titulo-grid.component';

@Component({
  selector: 'gi-titulo',
  imports: [TituloDetalheComponent, TituloGridComponent],
  providers: [],
  templateUrl: './titulo.component.html',
  styleUrl: './titulo.component.css',
})
export class TituloComponent {
  viewMode: ViewMode = ViewMode.GRID;
  detailId: string | number = 'add';

  toggleView() {
    this.viewMode =
      this.viewMode === ViewMode.GRID ? ViewMode.DETAIL : ViewMode.GRID;
  }

  openDetail($event: string | number) {
    this.detailId = $event;
    this.toggleView();
  }
}
