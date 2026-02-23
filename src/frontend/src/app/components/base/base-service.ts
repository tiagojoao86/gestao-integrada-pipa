/* eslint-disable @angular-eslint/prefer-inject */
import {
  HttpClient,
  HttpErrorResponse,
  HttpHeaders,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EMPTY, Observable, take } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { PageRequest } from './model/page-request';
import {
  Response,
  ResponseList,
  ResponseListNoPagination,
  ResponseString,
} from './model/response';
import { HttpConstants } from './constants/http-constants';
import { MessageService } from './messages/messages.service';
import { AuditInfoData } from './audit-info/audit-info.component';

export interface ExecutionCallbacks<T> {
  onSuccess: (data: T) => void;
  onError?: (error: HttpErrorResponse) => void;
}

@Injectable()
export abstract class BaseService<D, G = D> {
  urlBase = '/api/';

  protected httpClient: HttpClient;
  protected messageService: MessageService;

  constructor(httpClient: HttpClient, messageService: MessageService) {
    this.httpClient = httpClient;
    this.messageService = messageService;
  }

  abstract getDomain(): string;

  list(request: PageRequest): Observable<ResponseList<G>> {
    return this.httpClient
      .post<ResponseList<G>>(this.getUrl(HttpConstants.R_QUERY), request, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((response: ResponseList<G>) => {
          if (response?.body) {
            const content = response.body.content || [];
            const converted = content.map((c: G) => this.convertToGrid(c));
            return {
              ...response,
              body: { ...response.body, content: converted },
            } as ResponseList<G>;
          }
          return response;
        }),
        take(1)
      );
  }

  listAll(request: PageRequest): Observable<ResponseListNoPagination<G>> {
    return this.httpClient
      .post<ResponseListNoPagination<G>>(this.getUrl(HttpConstants.R_LIST), request, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((response: ResponseListNoPagination<G>) => {
          if (response?.body) {
            const content = response.body || [];
            const converted = content.map((c: G) => this.convertToGrid(c));
            return {
              ...response,
              body: converted,
            } as ResponseListNoPagination<G>;
          }
          return response;
        }),
        take(1)
      );
  }

  save(dto: D, callbacks: ExecutionCallbacks<D>) {
    dto = this.convertToPlain(dto);
    this.httpClient
      .post<Response<D>>(this.getUrl(), dto, { headers: this.getHeaders() })
      .pipe(take(1))
      .subscribe({
        next: (response: Response<D>) => {
          if (response.body) {
            const body = response.body;
            const converted = this.convertToDto(body);
            callbacks.onSuccess(converted as D);
          }
        },
        error: (error: HttpErrorResponse) => {
          if (callbacks.onError) {
            callbacks.onError(error);
          } else this.handleError(error);
        },
      });
  }

  findById(id: string): Observable<Response<D>> {
    return this.httpClient
      .get<Response<D>>(this.getUrl(HttpConstants.R_FIND_BY_ID), {
        headers: this.getHeaders(),
        params: { id: id },
      })
      .pipe(
        map((response: Response<D>) => {
          if (response?.body) {
            const converted = this.convertToDto(response.body);
            return { ...response, body: converted } as Response<D>;
          }
          return response;
        }),
        take(1)
      );
  }

  protected abstract convertToDto(body: D): D;

  protected abstract convertToGrid(item: G): G;

  protected convertToPlain(item: D): D {
    return item;
  }

  delete(id: string): Observable<ResponseString> {
    return this.httpClient
      .delete<ResponseString>(this.getUrl('/' + id), {
        headers: this.getHeaders(),
      })
      .pipe(
        take(1),
        catchError((error: HttpErrorResponse) => {
          this.handleError(error);
          return EMPTY;
        })
      );
  }

  getAuditInfo(id: string): Observable<Response<AuditInfoData>> {
    return this.httpClient
      .get<Response<AuditInfoData>>(this.getUrl(`/${id}/audit-info`))
      .pipe(take(1));
  }

  getUrl(contexto = ''): string {
    return this.urlBase + this.getDomain() + contexto;
  }

  getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
    });
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error?.messages?.length > 0) {
      this.messageService.erro(error.error.messages);
    } else {
      const genericMessage =
        error.error?.title ||
        $localize`:@@erro.generico.inesperado:Ocorreu um erro inesperado.`;
      this.messageService.erro(genericMessage);
    }
  }
}
