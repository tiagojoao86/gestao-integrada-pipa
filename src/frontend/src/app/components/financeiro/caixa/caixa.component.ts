import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { CaixaGridComponent } from './caixa-grid/caixa-grid.component';
import { CaixaDetalheComponent } from './caixa-detalhe/caixa-detalhe.component';

@Component({
  selector: 'gi-caixa',
  imports: [CaixaGridComponent, CaixaDetalheComponent],
  templateUrl: './caixa.component.html',
  styleUrl: './caixa.component.css',
})
export class CaixaComponent {
  viewMode: ViewMode = ViewMode.GRID;
  detailId = 'add';

  toggleView() {
    this.viewMode = this.viewMode === ViewMode.GRID ? ViewMode.DETAIL : ViewMode.GRID;
  }

  openDetail($event: string) {
    this.detailId = $event;
    this.toggleView();
  }
}
