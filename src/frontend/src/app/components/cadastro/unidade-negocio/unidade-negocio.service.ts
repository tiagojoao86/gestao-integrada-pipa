import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { UnidadeNegocioDTO } from './model/unidade-negocio-dto';
import { MessageService } from '../../base/messages/messages.service';
import { UnidadeNegocioBackendMessages } from './unidade-negocio-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class UnidadeNegocioService extends BaseService<UnidadeNegocioDTO> {
  private static readonly UNIDADE_NEGOCIO = 'unidade-negocio';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(UnidadeNegocioBackendMessages)
    );
  }

  getDominio(): string {
    return UnidadeNegocioService.UNIDADE_NEGOCIO;
  }

  listarParaVinculo(): Observable<
    { id: string; nome: string; codigo: string }[]
  > {
    return this.httpClient
      .get<{ body: UnidadeNegocioDTO[] }>(this.getUrl('/ativas'))
      .pipe(
        map((response) =>
          response.body.map((u) => ({
            id: u.id!,
            nome: u.nome,
            codigo: u.codigo,
          }))
        ),
        take(1)
      );
  }
}
