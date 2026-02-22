import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { TituloCategoriaDTO } from './model/titulo-categoria.dto';
import { TituloCategoriaGridDTO } from './model/titulo-categoria-grid.dto';
import { instanceToPlain, plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class TituloCategoriaService extends BaseService<
  TituloCategoriaDTO,
  TituloCategoriaGridDTO
> {
  private static readonly DOMINIO = 'titulo-categoria';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
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

  protected override convertToPlain(
    item: TituloCategoriaDTO
  ): TituloCategoriaDTO {
    // Ensure it's a proper class instance using the factory method
    const instance = TituloCategoriaDTO.from(item);
    // Then convert to plain object with transformations applied
    return instanceToPlain(instance, {
      enableCircularCheck: true,
      exposeDefaultValues: true,
    }) as TituloCategoriaDTO;
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
