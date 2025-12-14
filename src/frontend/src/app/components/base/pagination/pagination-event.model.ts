export class PaginationEvent {
  static readonly DEFAULT_PAGE_SIZE: number = 15;

  pageNumber: number;
  totalPages: number;
  itemsPerPage: number;

  constructor(pageNumber: number, totalPages: number, itemsPerPage: number) {
    this.pageNumber = pageNumber;
    this.totalPages = totalPages;
    this.itemsPerPage = itemsPerPage;
  }
}
