import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { TabelaDetalheComponent } from './detalhe/tabela-detalhe.component';
import { TabelaGridComponent } from './grid/tabela-grid.component';

@Component({
  selector: 'gi-tabela',
  imports: [TabelaDetalheComponent, TabelaGridComponent],
  templateUrl: './tabela.component.html',
  styleUrl: './tabela.component.css',
})
export class TabelaComponent {
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
