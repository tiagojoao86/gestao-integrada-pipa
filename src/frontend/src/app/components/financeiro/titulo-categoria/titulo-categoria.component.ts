import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { CategoriaTituloDetalheComponent } from './titulo-categoria-detalhe/titulo-categoria-detalhe.component';
import { TituloCategoriaGridComponent } from './titulo-categoria-grid/titulo-categoria-grid.component';

@Component({
  selector: 'gi-titulo-categoria',
  imports: [CategoriaTituloDetalheComponent, TituloCategoriaGridComponent],
  templateUrl: './titulo-categoria.component.html',
  styleUrls: ['./titulo-categoria.component.css'],
})
export class TituloCategoriaComponent {
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
