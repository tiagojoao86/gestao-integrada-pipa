import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AutoCompleteModule, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import type { EntitySearchConfig, ResultField } from '../entity-search/entity-search.model';
import { FilterDTO, FilterItem, FilterOperator, FilterLogicOperator } from '../model/filter-dto';
import { PageRequest } from '../model/page-request';

@Component({
  selector: 'gi-entity-field',
  standalone: true,
  imports: [FormsModule, AutoCompleteModule],
  templateUrl: './entity-field.component.html',
  styleUrl: './entity-field.component.css',
})
export class EntityFieldComponent implements OnDestroy {
  @Input() label = '';
  @Input() entityLabel: string | null = null;
  @Input() searchConfig?: EntitySearchConfig<unknown>;
  @Output() entitySearch = new EventEmitter<void>();
  @Output() remove = new EventEmitter<void>();
  @Output() entitySelected = new EventEmitter<unknown>();

  autocompleteQuery = '';
  suggestions: unknown[] = [];

  private destroy$ = new Subject<void>();

  onCompleteMethod(event: AutoCompleteCompleteEvent): void {
    const query = event.query;
    if (!this.searchConfig || query.length < 2) {
      this.suggestions = [];
      return;
    }

    const items: FilterItem[] = this.searchConfig.searchFields.map((field) => ({
      property: field.key,
      operator: FilterOperator.CONTAINS.key,
      values: [query],
    }));

    const filterDTO = new FilterDTO(FilterLogicOperator.OR.getKey(), items, false);
    const pageRequest = new PageRequest(filterDTO, 8, 0, []);

    this.searchConfig.service
      .list(pageRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.suggestions = response.body?.content ?? [];
        },
        error: () => {
          this.suggestions = [];
        },
      });
  }

  onEntitySelect(item: unknown): void {
    this.entitySelected.emit(item);
    this.autocompleteQuery = '';
    this.suggestions = [];
  }

  getItemLabel(item: unknown): string {
    if (!this.searchConfig || !item) return '';
    return this.searchConfig.resultFields
      .map((field) => this.getFieldValue(item, field))
      .filter((v) => !!v)
      .join(' — ');
  }

  private getFieldValue(entity: unknown, field: ResultField): string {
    const keys = field.key.split('.');
    let value: unknown = entity;
    for (const key of keys) {
      if (value && typeof value === 'object' && key in value) {
        value = (value as Record<string, unknown>)[key];
      } else {
        return '';
      }
    }
    return value != null ? String(value) : '';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
