import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { TituloCategoriaDTO } from './model/titulo-categoria.dto';
import { TituloCategoriaGridDTO } from './model/titulo-categoria-grid.dto';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { TituloCategoriaBackendMessageService } from './titulo-categoria-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class TituloCategoriaService extends BaseService<
  TituloCategoriaDTO,
  TituloCategoriaGridDTO
> {
  private static readonly DOMINIO = 'titulo-categoria';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(TituloCategoriaBackendMessageService)
    );
  }

  protected override convertToDto(body: unknown): TituloCategoriaDTO {
    return plainToInstance(
      TituloCategoriaDTO,
      body as object
    ) as TituloCategoriaDTO;
  }

  protected override convertToGrid(
    item: TituloCategoriaGridDTO
  ): TituloCategoriaGridDTO {
    return plainToInstance(
      TituloCategoriaGridDTO,
      item as object
    ) as TituloCategoriaGridDTO;
  }

  getDomain(): string {
    return TituloCategoriaService.DOMINIO;
  }

  // Example helper if needed in future
  listarSimples(): Observable<TituloCategoriaDTO[]> {
    return this.httpClient
      .get<{ body: TituloCategoriaDTO[] }>(this.getUrl('/simples'))
      .pipe(
        map((r) => r.body),
        take(1)
      );
  }
}
