import { Component } from '@angular/core';
import { MovimentacaoFinanceiraGridComponent } from './movimentacao-financeira-grid/movimentacao-financeira-grid.component';
import { MovimentacaoFinanceiraDetalheComponent } from './movimentacao-financeira-detalhe/movimentacao-financeira-detalhe.component';

@Component({
  selector: 'gi-movimentacao-financeira',
  templateUrl: './movimentacao-financeira.component.html',
  styleUrls: ['./movimentacao-financeira.component.css'],
  imports: [
    MovimentacaoFinanceiraGridComponent,
    MovimentacaoFinanceiraDetalheComponent,
  ],
})
export class MovimentacaoFinanceiraComponent {
  viewMode: 'GRID' | 'DETAIL' = 'GRID';
  detailId: string | null = null;

  openDetail(id: string) {
    this.detailId = id;
    this.viewMode = 'DETAIL';
  }

  backToGrid() {
    this.viewMode = 'GRID';
    this.detailId = null;
  }
}
