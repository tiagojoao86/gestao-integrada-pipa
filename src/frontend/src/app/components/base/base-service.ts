/* eslint-disable @angular-eslint/prefer-inject */
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, take } from 'rxjs';
import { PageRequest } from './model/page-request';
import { Response } from './model/response';
import { HttpConstants } from './constants/http-constants';
import { MessageService } from './messages/messages.service';
import { AbstractBackendMessageService } from './services/backend-messsages/abstract-backend-message.service';

export interface ExecutionCallbacks<T> {
  onSuccess: (data: T) => void;
  onError?: (error: HttpErrorResponse) => void;
}

@Injectable()
export abstract class BaseService<D> {
  urlBase = '/api/';

  private httpClient: HttpClient;
  private messageService: MessageService;
  private backendMessageService: AbstractBackendMessageService;


  constructor(
    httpClient: HttpClient,
    messageService: MessageService,
    backendMessageService: AbstractBackendMessageService
  ) {
    this.httpClient = httpClient;
    this.messageService = messageService;
    this.backendMessageService = backendMessageService;
  }

  abstract getDominio(): string;

  list(request: PageRequest): Observable<Response> {
    return this.httpClient
      .post<Response>(this.getUrl(HttpConstants.R_QUERY), request, {
        headers: this.getHeaders(),
      })
      .pipe(take(1));
  }

  save(dto: D, callbacks: ExecutionCallbacks<D>) {
    this.httpClient
      .post<Response>(this.getUrl(), dto, { headers: this.getHeaders() })
      .pipe(take(1))
      .subscribe({
        next: (response) => {
          callbacks.onSuccess(response.body);
        },
        error: (error: HttpErrorResponse) => {
          if (callbacks.onError) {
            callbacks.onError(error);
          } else this.handleError(error);
        },
      });
  }

  findById(id: string): Observable<Response> {
    return this.httpClient
      .get<Response>(this.getUrl(HttpConstants.R_FIND_BY_ID), {
        headers: this.getHeaders(),
        params: { id: id },
      })
      .pipe(take(1));
  }

  delete(id: string): Observable<Response> {
    return this.httpClient
      .delete<Response>(this.getUrl('/' + id), {
        headers: this.getHeaders(),
      })
      .pipe(take(1));
  }

  getUrl(contexto = ''): string {
    return this.urlBase + this.getDominio() + contexto;
  }

  getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
    });
  }

  private handleError(error: HttpErrorResponse) {
    if (error.status === 400 && error.error?.userMessageKey) {
      const translatedErrors = this.backendMessageService.getMessages(
        error.error.userMessageKey
      );
      this.messageService.erro(translatedErrors);
    } else {
      const genericMessage =
        error.error?.title ||
        $localize`:@@erro.generico.inesperado:Ocorreu um erro inesperado.`;
      this.messageService.erro(genericMessage);
    }
  }
}
