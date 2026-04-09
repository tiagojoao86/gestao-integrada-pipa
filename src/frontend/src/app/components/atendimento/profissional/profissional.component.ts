import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { ProfissionalDetalheComponent } from './detalhe/profissional-detalhe.component';
import { ProfissionalGridComponent } from './grid/profissional-grid.component';

@Component({
  selector: 'gi-profissional',
  imports: [ProfissionalDetalheComponent, ProfissionalGridComponent],
  templateUrl: './profissional.component.html',
  styleUrl: './profissional.component.css',
})
export class ProfissionalComponent {
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
