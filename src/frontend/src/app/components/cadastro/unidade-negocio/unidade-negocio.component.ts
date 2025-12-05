import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ViewMode } from '../../base/model/view-mode.enum';
import { UnidadeNegocioGridComponent } from './unidade-negocio-grid/unidade-negocio-grid.component';
import { UnidadeNegocioDetalheComponent } from './unidade-negocio-detalhe/unidade-negocio-detalhe.component';

@Component({
  selector: 'gi-unidade-negocio',
  imports: [
    CommonModule,
    UnidadeNegocioDetalheComponent,
    UnidadeNegocioGridComponent,
  ],
  providers: [],
  templateUrl: './unidade-negocio.component.html',
  styleUrl: './unidade-negocio.component.css',
})
export class UnidadeNegocioComponent {
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
