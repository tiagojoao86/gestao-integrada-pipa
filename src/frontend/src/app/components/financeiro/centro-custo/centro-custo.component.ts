import { Component } from '@angular/core';

import { ViewMode } from '../../base/model/view-mode.enum';
import { CentroCustoDetalheComponent } from './detalhe/centro-custo-detalhe.component';
import { CentroCustoGridComponent } from './grid/centro-custo-grid.component';

@Component({
  selector: 'gi-centro-custo',
  imports: [CentroCustoDetalheComponent, CentroCustoGridComponent],
  templateUrl: './centro-custo.component.html',
  styleUrl: './centro-custo.component.css',
})
export class CentroCustoComponent {
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
