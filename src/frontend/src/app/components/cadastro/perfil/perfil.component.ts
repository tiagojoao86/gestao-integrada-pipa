import { Component } from '@angular/core';

import { ViewMode } from '../../base/model/view-mode.enum';
import { PerfilDetalheComponent } from './perfil-detalhe/perfil-detalhe.component';
import { PerfilGridComponent } from './perfil-grid/perfil-grid.component';

@Component({
  selector: 'gi-perfil',
  imports: [PerfilDetalheComponent, PerfilGridComponent],
  providers: [],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css',
  standalone: true,
})
export class PerfilComponent {
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

