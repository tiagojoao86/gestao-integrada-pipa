import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { TabelaRegraDetalheComponent } from './detalhe/tabela-regra-detalhe.component';
import { TabelaRegraGridComponent } from './grid/tabela-regra-grid.component';

@Component({
  selector: 'gi-tabela-regra',
  imports: [TabelaRegraDetalheComponent, TabelaRegraGridComponent],
  templateUrl: './tabela-regra.component.html',
})
export class TabelaRegraComponent {
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
