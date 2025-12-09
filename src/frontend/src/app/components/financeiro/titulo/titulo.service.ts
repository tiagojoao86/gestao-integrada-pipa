import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { TituloDTO } from './model/titulo-dto';
import { MessageService } from '../../base/messages/messages.service';
import { TituloBackendMessages } from './titulo-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class TituloService extends BaseService<TituloDTO> {
  private static readonly TITULO = 'titulo';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(TituloBackendMessages)
    );
  }

  getDominio(): string {
    return TituloService.TITULO;
  }

  listarUnidadesDisponiveis(): Observable<
    { id: string; nome: string; codigo: string }[]
  > {
    return this.httpClient
      .get<{ body: { id: string; nome: string; codigo: string }[] }>(
        this.getUrl('/unidades-disponiveis')
      )
      .pipe(
        map((response) => response.body),
        take(1)
      );
  }
}
