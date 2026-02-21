import { Component } from '@angular/core';

import { TituloDetalheComponent } from './titulo-detalhe/titulo-detalhe.component';
import { TituloGridComponent } from './titulo-grid/titulo-grid.component';
import { TituloGridDTO } from './model/titulo-grid-dto';
import { MovimentacaoFinanceiraDetalheComponent } from '../movimentacao-financeira/movimentacao-financeira-detalhe/movimentacao-financeira-detalhe.component';

@Component({
  selector: 'gi-titulo',
  imports: [
    TituloDetalheComponent,
    TituloGridComponent,
    MovimentacaoFinanceiraDetalheComponent,
  ],
  providers: [],
  templateUrl: './titulo.component.html',
  styleUrl: './titulo.component.css',
})
export class TituloComponent {
  viewMode: 'GRID' | 'DETAIL' | 'MOVIMENTACAO' = 'GRID';
  detailId: string | number = 'add';
  titulosSelecionados: TituloGridDTO[] = [];

  toggleView() {
    this.viewMode = this.viewMode === 'GRID' ? 'DETAIL' : 'GRID';
  }

  openDetail($event: string | number) {
    this.detailId = $event;
    this.toggleView();
  }

  openMovimentacao(titulos: TituloGridDTO[]) {
    this.titulosSelecionados = titulos;
    this.viewMode = 'MOVIMENTACAO';
  }

  backToGrid() {
    this.viewMode = 'GRID';
  }
}
