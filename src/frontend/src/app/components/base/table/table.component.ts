import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Order, Direction } from '../model/page-request';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SortMeta } from 'primeng/api';

@Component({
  selector: 'gi-table-component',
  imports: [CommonModule, TableModule, ButtonModule],
  templateUrl: './table.component.html',
  styleUrl: './table.component.css',
  providers: [],
})
export class TableComponent implements OnInit {
  @Input() dataSource: unknown[] = [];
  @Input() columnDefinition: DataSourceColumn[] = [];
  @Input() actions: Action[] = [];

  @Output() sortingEvent = new EventEmitter<Order[]>();

  columns: string[] = [];

  ngOnInit(): void {
    this.columns = this.columnDefinition.map((it) => it.name);

    if (this.actions) {
      this.columns.push('actions');
    }
  }

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

export interface DataSourceColumn {
  name: string;
  label: string;
  // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
  getValue: Function;
}

export interface Action {
  icon: string;
  // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
  action: Function;
}
