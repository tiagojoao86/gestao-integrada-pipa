import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  EntitySearchConfig,
  SearchField,
  ResultField,
} from './entity-search.model';
import { FilterDTO, FilterItem, FilterOperator } from '../model/filter-dto';
import { PageRequest } from '../model/page-request';
import { PaginationComponent } from '../pagination/pagination.component';
import { PaginationEvent } from '../pagination/pagination-event.model';
import { InputText } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { TableComponent } from '../table/table.component';
import { ColumnModel } from '../table/column.model';
import { ActionModel } from '../table/action.model';

@Component({
  selector: 'gi-entity-search',
  imports: [
    CommonModule,
    FormsModule,
    PaginationComponent,
    InputText,
    SelectModule,
    ButtonModule,
    TableComponent,
  ],
  templateUrl: './entity-search.component.html',
  styleUrl: './entity-search.component.css',
  standalone: true,
})
export class EntitySearchComponent<T = unknown> implements OnInit {
  @Output() entitySelected = new EventEmitter<T>();
  @Output() searchCancelled = new EventEmitter<void>();

  isVisible = false;
  config: EntitySearchConfig<T> | null = null;

  selectedSearchField: SearchField | null = null;
  searchValue = '';
  isSearching = false;

  searchResults: unknown[] = [];
  totalElements = 0;
  currentPage = 0;
  pageSize = 10;

  tableColumns: ColumnModel<unknown>[] = [];
  tableActions: ActionModel<unknown>[] = [];

  ngOnInit(): void {
    if (this.config) {
      this.resetSearch();
      this.pageSize = this.config.pageSize || 10;
      if (this.config.searchFields && this.config.searchFields.length > 0) {
        this.selectedSearchField = this.config.searchFields[0];
      }
      this.buildTableConfig();
    }
  }

  private buildTableConfig(): void {
    if (!this.config) return;

    // Converte ResultField[] para ColumnModel[]
    this.tableColumns = this.config.resultFields.map((field) => ({
      name: field.key,
      label: field.label,
      getValue: (rowData: unknown) => this.getFieldValue(rowData, field),
    }));

    // Configura a ação de selecionar
    this.tableActions = [
      {
        icon: 'add_notes',
        title: $localize`Selecionar`,
        action: (rowData: unknown) => this.onSelectEntity(rowData),
      },
    ];
  }

  private resetSearch(): void {
    this.selectedSearchField = null;
    this.searchValue = '';
    this.searchResults = [];
    this.totalElements = 0;
    this.currentPage = 0;
    this.isSearching = false;
  }

  onSearch(): void {
    if (!this.selectedSearchField || !this.searchValue.trim() || !this.config) {
      return;
    }

    this.currentPage = 0;
    this.performSearch();
  }

  onPageChange(event: PaginationEvent): void {
    this.currentPage = event.pageNumber;
    this.pageSize = event.itemsPerPage;
    this.performSearch();
  }

  private performSearch(): void {
    if (!this.config || !this.selectedSearchField) {
      return;
    }

    this.isSearching = true;

    // Monta o FilterDTO
    const filterItem: FilterItem = {
      property: this.selectedSearchField.key,
      operator: FilterOperator.CONTAINS.key,
      values: [this.searchValue],
    };

    const filterDTO: FilterDTO = {
      items: [filterItem],
    };

    // Monta o PageRequest
    const pageRequest: PageRequest = {
      page: this.currentPage,
      size: this.pageSize,
      order: [],
      filter: filterDTO,
    };

    // Chama o service
    this.config.service.list(pageRequest).subscribe({
      next: (response) => {
        this.searchResults = response.body?.content || [];
        this.totalElements = response.body?.totalElements || 0;
        this.isSearching = false;
      },
      error: () => {
        this.searchResults = [];
        this.totalElements = 0;
        this.isSearching = false;
      },
    });
  }

  onSelectEntity(entity: unknown): void {
    this.entitySelected.emit(entity as T);
  }

  onCancel(): void {
    this.searchCancelled.emit();
  }

  onOverlayClick(): void {
    // Não fecha ao clicar no overlay por padrão
    // Para permitir fechar clicando fora, descomente:
    // this.onCancel();
  }

  getFieldValue(entity: unknown, field: ResultField): string {
    // Suporta propriedades aninhadas (ex: 'endereco.cidade')
    const keys = field.key.split('.');
    let value: unknown = entity;

    for (const key of keys) {
      if (value && typeof value === 'object' && key in value) {
        value = (value as Record<string, unknown>)[key];
      } else {
        return '';
      }
    }

    return value !== null && value !== undefined ? String(value) : '';
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements / this.pageSize);
  }

  get hasResults(): boolean {
    return this.searchResults.length > 0;
  }

  get showNoResults(): boolean {
    return !this.isSearching && !this.hasResults;
  }
}
