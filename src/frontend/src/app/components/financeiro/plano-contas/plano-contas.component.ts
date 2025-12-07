import { Component } from '@angular/core';

import { ViewMode } from '../../base/model/view-mode.enum';
import { PlanoContasGridComponent } from './plano-contas-grid/plano-contas-grid.component';
import { PlanoContasDetalheComponent } from './plano-contas-detalhe/plano-contas-detalhe.component';

@Component({
  selector: 'gi-plano-contas',
  imports: [
    PlanoContasDetalheComponent,
    PlanoContasGridComponent
],
  providers: [],
  templateUrl: './plano-contas.component.html',
  styleUrl: './plano-contas.component.css',
})
export class PlanoContasComponent {
  viewMode: ViewMode = ViewMode.GRID;
  detailId = 'add';

  toggleView() {
    this.viewMode =
      this.viewMode === ViewMode.GRID ? ViewMode.DETAIL : ViewMode.GRID;
  }

  openDetail($event: string) {
    this.detailId = $event;
    this.toggleView();
  }
}
