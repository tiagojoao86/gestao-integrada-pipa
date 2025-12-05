import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ViewMode } from '../../base/model/view-mode.enum';
import { ContaBancariaDetalheComponent } from './conta-bancaria-detalhe/conta-bancaria-detalhe.component';
import { ContaBancariaGridComponent } from './conta-bancaria-grid/conta-bancaria-grid.component';

@Component({
  selector: 'gi-conta-bancaria',
  imports: [
    CommonModule,
    ContaBancariaDetalheComponent,
    ContaBancariaGridComponent,
  ],
  providers: [],
  templateUrl: './conta-bancaria.component.html',
  styleUrl: './conta-bancaria.component.css',
})
export class ContaBancariaComponent {
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
