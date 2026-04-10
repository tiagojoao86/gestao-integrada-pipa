import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { ConvenioDetalheComponent } from './detalhe/convenio-detalhe.component';
import { ConvenioGridComponent } from './grid/convenio-grid.component';

@Component({
  selector: 'gi-convenio',
  imports: [ConvenioDetalheComponent, ConvenioGridComponent],
  templateUrl: './convenio.component.html',
  styleUrl: './convenio.component.css',
})
export class ConvenioComponent {
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
