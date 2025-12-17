import { Component } from '@angular/core';

import { ViewMode } from '../../base/model/view-mode.enum';
import { SetorDetalheComponent } from './detalhe/setor-detalhe.component';
import { SetorGridComponent } from './grid/setor-grid.component';

@Component({
  selector: 'gi-setor',
  imports: [SetorDetalheComponent, SetorGridComponent],
  templateUrl: './setor.component.html',
  styleUrl: './setor.component.css',
})
export class SetorComponent {
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
