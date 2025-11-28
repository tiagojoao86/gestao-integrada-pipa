import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';

@Component({
  selector: 'gi-paginator-component',
  imports: [CommonModule, ButtonModule, SelectModule, FormsModule],
  templateUrl: './paginator.component.html',
  styleUrl: './paginator.component.css',
})
export class PaginatorComponent {
  @Output() paginationEvent =
    new EventEmitter<PaginationEvent>();

  @Input() pageNumber = 0;
  @Input() totalRegisters = 1;
  @Input() itemsPerPage = PaginationEvent.DEFAULT_PAGE_SIZE;

  itemsPerPageList = [5, 10, 15, 25, 50];

  firstPage() {
    if (this.pageNumber === 0) {
      return;
    }

    this.pageNumber = 0;
    this.paginationEvent.emit(this.getPaginationEvent());
  }

  previousPage() {
    if (this.pageNumber === 0) {
      return;
    }

    this.pageNumber--;
    this.paginationEvent.emit(this.getPaginationEvent());
  }

  nextPage() {
    if (this.pageNumber === this.getTotalPages() - 1) {
      return;
    }

    this.pageNumber++;
    this.paginationEvent.emit(this.getPaginationEvent());
  }

  lastPage() {
    if (this.pageNumber === this.getTotalPages() - 1) {
      return;
    }

    this.pageNumber = this.getTotalPages() - 1;
    this.paginationEvent.emit(this.getPaginationEvent());
  }

  getPaginationEvent(): PaginationEvent {
    return {
      pageNumber: this.pageNumber,
      totalPages: this.getTotalPages(),
      itemsPerPage: this.itemsPerPage,
    };
  }

  changeItemsPerPage(value: number) {
    this.itemsPerPage = value;
    this.paginationEvent.emit(this.getPaginationEvent());
  }

  getTotalPages() {
    return Math.ceil(this.totalRegisters / this.itemsPerPage);
  }
}

export class PaginationEvent {
  static readonly DEFAULT_PAGE_SIZE: number = 5;

  pageNumber: number;
  totalPages: number;
  itemsPerPage: number;

  constructor(pageNumber: number, totalPages: number, itemsPerPage: number) {
    this.pageNumber = pageNumber;
    this.totalPages = totalPages;
    this.itemsPerPage = itemsPerPage;
  }
}
