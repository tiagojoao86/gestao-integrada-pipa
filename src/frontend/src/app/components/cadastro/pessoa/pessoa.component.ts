import { Component } from '@angular/core';

import { ViewMode } from '../../base/model/view-mode.enum';
import { PessoaDetalheComponent } from './pessoa-detalhe/pessoa-detalhe.component';
import { PessoaGridComponent } from './pessoa-grid/pessoa-grid.component';

@Component({
  selector: 'gi-pessoa',
  imports: [PessoaDetalheComponent, PessoaGridComponent],
  providers: [],
  templateUrl: './pessoa.component.html',
  styleUrl: './pessoa.component.css',
})
export class PessoaComponent {
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
