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
export class FiltroComponent implements OnInit {
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
    if (FilterType.TEXTO === tipo) {
      return [
        FilterOperator.CONTAINS,
        FilterOperator.NOT_CONTAINS,
        FilterOperator.NEQ,
        FilterOperator.EQ,
      ];
    }
    if (FilterType.DATA === tipo || FilterType.NUMERO === tipo) {
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
    if (FilterType.SELECAO === tipo) {
      return [
        FilterOperator.NEQ,
        FilterOperator.EQ,
        FilterOperator.GT,
        FilterOperator.GE,
        FilterOperator.LT,
        FilterOperator.LE,
      ];
    }
    if (FilterType.MULTI_SELECAO === tipo) {
      return [FilterOperator.IN, FilterOperator.NOT_IN];
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
    if (FilterType.TEXTO === tipo) {
      return new FormControl(FilterOperator.CONTAINS);
    }

    if (FilterType.DATA === tipo || FilterType.NUMERO === tipo) {
      return new FormControl(FilterOperator.EQ);
    }

    if (FilterType.SELECAO === tipo || FilterType.MULTI_SELECAO === tipo) {
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

    const itens: FilterItem[] = [];
    this.selectedFilters.forEach((selected) => {
      const operador = this.form
        .get(selected.property + '_operacao')
        ?.getRawValue().key;
      const valores = this.form.get(selected.property)?.getRawValue();

      if (selected.filterType === FilterType.DATA) {
        itens.push({
          property: selected.property,
          operator: operador,
          values: [valores ? valores : ''],
        });
      }

      if (selected.filterType === FilterType.TEXTO) {
        itens.push({
          property: selected.property,
          operator: operador,
          values: [valores ? valores + '' : ''],
        });
      }
    });
    this.doFilter.emit({
      items: itens,
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
  values?: FilterSelectedValues[];
}

export enum FilterType {
  TEXTO,
  MULTI_SELECAO,
  SELECAO,
  NUMERO,
  DATA,
}

export interface FilterSelectedValues {
  chave: string;
  label: string;
}

export interface FiltroCamposSelecao {
  nome: string;
  label: string;
}
