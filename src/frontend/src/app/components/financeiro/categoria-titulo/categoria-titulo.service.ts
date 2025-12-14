import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { CategoriaTituloDTO } from './model/categoria-titulo.dto';
import { MessageService } from '../../base/messages/messages.service';
import { CategoriaTituloBackendMessageService } from './categoria-titulo-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class CategoriaTituloService extends BaseService<CategoriaTituloDTO> {
  private static readonly DOMINIO = 'categoria-titulo';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(CategoriaTituloBackendMessageService)
    );
  }

  getDomain(): string {
    return CategoriaTituloService.DOMINIO;
  }

  // Example helper if needed in future
  listarSimples(): Observable<CategoriaTituloDTO[]> {
    return this.httpClient
      .get<{ body: CategoriaTituloDTO[] }>(this.getUrl('/simples'))
      .pipe(
        map((r) => r.body),
        take(1)
      );
  }
}
