import { FilterDTO } from './filter-dto';

export class PageRequest {
  filter: FilterDTO;
  page: number;
  size: number;
  order: Order[];

  constructor(filter: FilterDTO, size: number, page: number, sort: Order[]) {
    this.filter = filter;
    this.page = page;
    this.size = size;
    this.order = sort;
  }
}

export interface Pageable {
  page: number;
  size: number;
  sort: string | null;
}

export interface Order {
  direction: Direction;
  property: string;
}

export enum Direction {
  ASC,
  DESC,
}
