/**
 * Response for single-entity payloads
 * Keeps the same structure expected from backend.
 */
export interface Response<D> {
  body: D | null;
  statusCode: number;
  erroMessage: string | null;
}

/**
 * Response wrapper for list/page payloads coming from backend.
 * body contains paging metadata (`content`, `totalElements`).
 */
export interface ResponseList<G> {
  body: {
    content: G[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
  } | null;
  statusCode: number;
  erroMessage: string | null;
}

export interface ResponseListNoPagination<G> {
  body: G[];
  statusCode: number;
  erroMessage: string | null;
}

export interface ResponseString {
  body: string | null;
  statusCode: number;
  erroMessage: string | null;
}
