import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MessageService } from '../base/messages/messages.service';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * Serviço de dashboards.
 * Não estende BaseService pois os endpoints de dashboard não seguem o padrão CRUD.
 * Cada quadro adicionado ao sistema gera um novo método nesta classe.
 */
@Injectable()
export class DashboardService {
  private static readonly DOMAIN = 'dashboard';

  protected httpClient = inject(HttpClient);
  protected messageService = inject(MessageService);

  getUrl(path = ''): string {
    return '/api/' + DashboardService.DOMAIN + path;
  }

  protected getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
    });
  }

  protected handleError(error: HttpErrorResponse): void {
    if (error.error?.messages?.length > 0) {
      this.messageService.erro(error.error.messages);
    } else {
      const msg =
        error.error?.title ||
        $localize`:@@erro.generico.inesperado:Ocorreu um erro inesperado.`;
      this.messageService.erro(msg);
    }
  }
}
