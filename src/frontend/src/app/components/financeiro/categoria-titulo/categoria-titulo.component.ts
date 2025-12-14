import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { CategoriaTituloDetalheComponent } from './categoria-titulo-detalhe/categoria-titulo-detalhe.component';
import { CategoriaTituloGridComponent } from './categoria-titulo-grid/categoria-titulo-grid.component';

@Component({
  selector: 'gi-categoria-titulo',
  imports: [CategoriaTituloDetalheComponent, CategoriaTituloGridComponent],
  templateUrl: './categoria-titulo.component.html',
  styleUrls: ['./categoria-titulo.component.css'],
})
export class CategoriaTituloComponent {
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
