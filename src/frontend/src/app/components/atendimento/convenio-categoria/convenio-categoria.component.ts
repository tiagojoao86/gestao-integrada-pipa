import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { ConvenioCategoriaDetalheComponent } from './detalhe/convenio-categoria-detalhe.component';
import { ConvenioCategoriaGridComponent } from './grid/convenio-categoria-grid.component';

@Component({
  selector: 'gi-convenio-categoria',
  imports: [ConvenioCategoriaDetalheComponent, ConvenioCategoriaGridComponent],
  templateUrl: './convenio-categoria.component.html',
  styleUrl: './convenio-categoria.component.css',
})
export class ConvenioCategoriaComponent {
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
