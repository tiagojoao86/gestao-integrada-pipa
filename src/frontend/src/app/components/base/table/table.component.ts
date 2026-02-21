import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewEncapsulation,
} from '@angular/core';
import { Order, Direction } from '../model/page-request';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { FormsModule } from '@angular/forms';
import { SortMeta } from 'primeng/api';
import { ColumnModel } from './column.model';
import { ActionModel } from './action.model';
import { NgClass } from '@angular/common';

@Component({
  selector: 'gi-table-component',
  imports: [TableModule, ButtonModule, CheckboxModule, FormsModule, NgClass],
  templateUrl: './table.component.html',
  styleUrl: './table.component.css',
  encapsulation: ViewEncapsulation.None,
  providers: [],
})
export class TableComponent<T> implements OnChanges {
  @Input() data: T[] = [];
  @Input() columns: ColumnModel<T>[] = [];
  @Input() actions: ActionModel<T>[] = [];
  @Input() sortable = true;
  @Input() multiSelection = false;
  @Input() pageIndex = 0;
  @Input() itemKey = 'id';

  sortType: 'single' | 'multiple' = 'multiple';

  @Output() sortingEvent = new EventEmitter<Order[]>();
  @Output() selectionChange = new EventEmitter<T[]>();

  private allSelectedIds = new Set<string>();
  private allSelectedItems = new Map<string, T>();

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.multiSelection) return;

    const dataChanged = 'data' in changes && !changes['data'].firstChange;
    const pageIndexChanged =
      'pageIndex' in changes &&
      !changes['pageIndex'].firstChange &&
      changes['pageIndex'].previousValue !== changes['pageIndex'].currentValue;

    if (dataChanged && !pageIndexChanged) {
      this.clearAllSelection();
    }
  }

  sortChange(multisortmeta: SortMeta[]): void {
    const ordem: Order[] = [];

    multisortmeta.forEach((sort: SortMeta) => {
      const property = sort.field;
      const direction = sort.order === 1 ? Direction.ASC : Direction.DESC;

      ordem.push({ property, direction });
    });

    this.sortingEvent.emit(ordem);
  }

  getItemKey(item: T): string {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return String((item as any)[this.itemKey]);
  }

  isSelected(item: T): boolean {
    return this.allSelectedIds.has(this.getItemKey(item));
  }

  isAllPageSelected(): boolean {
    if (!this.data || this.data.length === 0) return false;
    return this.data.every((item) => this.isSelected(item));
  }

  toggleItem(item: T): void {
    const key = this.getItemKey(item);
    if (this.allSelectedIds.has(key)) {
      this.allSelectedIds.delete(key);
      this.allSelectedItems.delete(key);
    } else {
      this.allSelectedIds.add(key);
      this.allSelectedItems.set(key, item);
    }
    this.selectionChange.emit(Array.from(this.allSelectedItems.values()));
  }

  toggleAll(): void {
    if (this.isAllPageSelected()) {
      this.data.forEach((item) => {
        const key = this.getItemKey(item);
        this.allSelectedIds.delete(key);
        this.allSelectedItems.delete(key);
      });
    } else {
      this.data.forEach((item) => {
        const key = this.getItemKey(item);
        this.allSelectedIds.add(key);
        this.allSelectedItems.set(key, item);
      });
    }
    this.selectionChange.emit(Array.from(this.allSelectedItems.values()));
  }

  private clearAllSelection(): void {
    this.allSelectedIds.clear();
    this.allSelectedItems.clear();
    this.selectionChange.emit([]);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  isDeletedRow(rowData: any): boolean {
    return rowData.deleted === true;
  }
}
