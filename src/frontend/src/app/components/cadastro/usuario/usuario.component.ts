import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ViewMode } from '../../base/model/view-mode.enum';
import { UsuarioDetalheComponent } from './usuario-detalhe/usuario-detalhe.component';
import { UsuarioGridComponent } from './usuario-grid/usuario-grid.component';

@Component({
  selector: 'gi-usuario',
  imports: [CommonModule, UsuarioDetalheComponent, UsuarioGridComponent],
  providers: [],
  templateUrl: './usuario.component.html',
  styleUrl: './usuario.component.css',
})
export class UsuariosComponent {
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
