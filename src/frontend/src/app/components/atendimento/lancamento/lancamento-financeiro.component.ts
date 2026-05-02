import { Component, OnInit } from '@angular/core';
import { LancamentoFinanceiroGridComponent } from './grid/lancamento-financeiro-grid.component';
import { LancamentoFinanceiroDetalheComponent } from './detalhe/lancamento-financeiro-detalhe.component';

type ViewMode = 'GRID' | 'DETAIL';

@Component({
  selector: 'gi-lancamento-financeiro',
  imports: [LancamentoFinanceiroGridComponent, LancamentoFinanceiroDetalheComponent],
  templateUrl: './lancamento-financeiro.component.html',
})
export class LancamentoFinanceiroComponent implements OnInit {
  viewMode: ViewMode = 'GRID';
  detailId: string | null = null;

  ngOnInit(): void {
    const state = history.state as { lancamentoId?: string };
    if (state?.lancamentoId) {
      this.detailId = state.lancamentoId;
      this.viewMode = 'DETAIL';
    }
  }

  openDetail(id: string): void {
    this.detailId = id;
    this.viewMode = 'DETAIL';
  }

  voltarParaGrid(): void {
    this.viewMode = 'GRID';
    this.detailId = null;
  }
}
