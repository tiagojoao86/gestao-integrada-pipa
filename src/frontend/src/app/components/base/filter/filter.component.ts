import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  FilterDTO,
  FilterItem,
  FilterOperator,
  FilterLogicOperator,
} from '../model/filter-dto';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { DatePicker } from 'primeng/datepicker';
@Component({
  selector: 'gi-filter-component',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    ButtonModule,
    SelectModule,
    DatePicker,
  ],
  templateUrl: './filter.component.html',
  styleUrl: './filter.component.css',
})
export class FilterComponent implements OnInit {
  readonly FilterType = FilterType;

  @Input() filters: FilterProperty[] = [];
  selectedFilters: FilterProperty[] = [];
  @Output() doFilter = new EventEmitter<FilterDTO>();
  @Output() doCancel = new EventEmitter<boolean>();

  operations: FilterOperator[] = FilterOperator.getAll();
  logicOperators: FilterLogicOperator[] = FilterLogicOperator.getAll();

  formSelector: FormGroup = new FormGroup([]);
  form: FormGroup = new FormGroup([]);

  ngOnInit(): void {
    this.formSelector.addControl('property', new FormControl());
    this.formSelector.addControl(
      'filterLogicOperator',
      new FormControl(FilterLogicOperator.AND)
    );
  }

  getOperacoes(tipo: FilterType): FilterOperator[] {
    if (FilterType.TEXT === tipo) {
      return [
        FilterOperator.CONTAINS,
        FilterOperator.NOT_CONTAINS,
        FilterOperator.NEQ,
        FilterOperator.EQ,
      ];
    }
    if (FilterType.DATE === tipo || FilterType.NUMBER === tipo) {
      return [
        FilterOperator.NEQ,
        FilterOperator.EQ,
        FilterOperator.GT,
        FilterOperator.GE,
        FilterOperator.LT,
        FilterOperator.LE,
        FilterOperator.BT,
      ];
    }
    if (FilterType.SELECT === tipo) {
      return [FilterOperator.NEQ, FilterOperator.EQ];
    }
    if (FilterType.MULTI_SELECT === tipo) {
      return [FilterOperator.IN, FilterOperator.NOT_IN];
    }
    if (FilterType.BOOLEAN === tipo) {
      return [FilterOperator.EQ, FilterOperator.NEQ];
    }

    return this.operations;
  }

  addFilter(property: FilterProperty) {
    if (this.selectedFilters.indexOf(property) === -1) {
      this.selectedFilters.push(property);
      this.filters.forEach((filtro) => {
        if (filtro.property === property.property) {
          this.form.addControl(filtro.property, new FormControl());
          this.form.addControl(
            filtro.property + '_operacao',
            this.buildDefaultOperation(property.filterType)
          );
        }
      });
    }
  }

  buildDefaultOperation(tipo: FilterType): FormControl {
    if (FilterType.TEXT === tipo) {
      return new FormControl(FilterOperator.CONTAINS);
    }

    if (FilterType.DATE === tipo || FilterType.NUMBER === tipo) {
      return new FormControl(FilterOperator.EQ);
    }

    if (FilterType.SELECT === tipo || FilterType.MULTI_SELECT === tipo) {
      return new FormControl(FilterOperator.IN);
    }

    return new FormControl();
  }

  removerFiltro(property: FilterProperty) {
    const index = this.selectedFilters.indexOf(property);
    if (index !== -1) {
      this.selectedFilters.splice(index, 1);
      this.onFiltrar();
    }
  }

  onFiltrar() {
    if (this.selectedFilters.length === 0) {
      this.doFilter.emit();
      return;
    }

    const items: FilterItem[] = [];
    this.selectedFilters.forEach((selected) => {
      const operador = this.form
        .get(selected.property + '_operacao')
        ?.getRawValue().key;
      const values = this.form.get(selected.property)?.getRawValue();

      if (selected.filterType === FilterType.DATE) {
        items.push({
          property: selected.property,
          operator: operador,
          values: [values ? values : ''],
        });
      }

      if (selected.filterType === FilterType.TEXT) {
        items.push({
          property: selected.property,
          operator: operador,
          values: [values ? values + '' : ''],
        });
      }

      if (
        selected.filterType === FilterType.SELECT ||
        selected.filterType === FilterType.BOOLEAN
      ) {
        items.push({
          property: selected.property,
          operator: operador,
          values: [values ? values.key + '' : ''],
        });
      }
    });
    this.doFilter.emit({
      items: items,
      filterLogicOperator: this.formSelector
        .get('filterLogicOperator')
        ?.getRawValue().key,
    });
  }

  onCancelar() {
    this.doCancel.emit();
  }
}

export interface FilterProperty {
  label: string;
  property: string;
  filterType: FilterType;
  options?: FilterOptions[];
}

export enum FilterType {
  TEXT,
  MULTI_SELECT,
  SELECT,
  NUMBER,
  DATE,
  BOOLEAN,
}

export interface FilterOptions {
  key: string;
  label: string;
}
