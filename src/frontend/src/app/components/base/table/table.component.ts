import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Order, Direction } from '../model/page-request';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SortMeta } from 'primeng/api';
import { ColumnModel } from './column.model';
import { ActionModel } from './action.model';

@Component({
  selector: 'gi-table-component',
  imports: [TableModule, ButtonModule],
  templateUrl: './table.component.html',
  styleUrl: './table.component.css',
  providers: [],
})
export class TableComponent<T> {
  @Input() data: T[] = [];
  @Input() columns: ColumnModel<T>[] = [];
  @Input() actions: ActionModel<T>[] = [];

  @Output() sortingEvent = new EventEmitter<Order[]>();
  
  sortChange(multisortmeta: SortMeta[]) {
    const ordem: Order[] = [];

    multisortmeta.forEach((sort: SortMeta) => {
      const property = sort.field;
      const direction = sort.order === 1 ? Direction.ASC : Direction.DESC;

      ordem.push({ property, direction });
    });

    this.sortingEvent.emit(ordem);
  }
}
