import { Component } from '@angular/core';
import { ViewMode } from '../../base/model/view-mode.enum';
import { CondicaoPagamentoDetalheComponent } from './condicao-pagamento-detalhe/condicao-pagamento-detalhe.component';
import { CondicaoPagamentoGridComponent } from './condicao-pagamento-grid/condicao-pagamento-grid.component';

@Component({
  selector: 'gi-condicao-pagamento',
  imports: [CondicaoPagamentoDetalheComponent, CondicaoPagamentoGridComponent],
  templateUrl: './condicao-pagamento.component.html',
  styleUrls: ['./condicao-pagamento.component.css'],
})
export class CondicaoPagamentoComponent {
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
