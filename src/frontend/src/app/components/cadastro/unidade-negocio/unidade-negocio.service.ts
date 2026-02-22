import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { UnidadeNegocioDTO } from './model/unidade-negocio-dto';
import { UnidadeNegocioGridDTO } from './model/unidade-negocio-grid-dto';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { plainToInstance } from 'class-transformer';

@Injectable()
export class UnidadeNegocioService extends BaseService<
  UnidadeNegocioDTO,
  UnidadeNegocioGridDTO
> {
  private static readonly UNIDADE_NEGOCIO = 'unidade-negocio';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return UnidadeNegocioService.UNIDADE_NEGOCIO;
  }

  listarParaVinculo(): Observable<
    { id: string; nome: string; codigo: string }[]
  > {
    return this.httpClient
      .get<{ body: UnidadeNegocioDTO[] }>(this.getUrl('/ativas'))
      .pipe(
        map((response) =>
          response.body.map((u: UnidadeNegocioGridDTO) => ({
            id: u.id,
            nome: u.nome,
            codigo: u.codigo,
          }))
        ),
        take(1)
      );
  }

  protected override convertToDto(body: unknown): UnidadeNegocioDTO {
    return plainToInstance(
      UnidadeNegocioDTO,
      body as object
    ) as UnidadeNegocioDTO;
  }

  protected override convertToGrid(
    item: UnidadeNegocioGridDTO
  ): UnidadeNegocioGridDTO {
    return plainToInstance(
      UnidadeNegocioGridDTO,
      item as object
    ) as UnidadeNegocioGridDTO;
  }
}
