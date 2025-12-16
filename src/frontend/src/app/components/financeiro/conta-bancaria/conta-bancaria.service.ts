import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { ContaBancariaDTO } from './model/conta-bancaria-dto';
import { ContaBancariaGridDTO } from './model/conta-bancaria-grid-dto';
import { MessageService } from '../../base/messages/messages.service';
import { ContaBancariaBackendMessages } from './conta-bancaria-backend-message.service';
import { BaseService } from '../../base/base-service';
import { plainToInstance } from 'class-transformer';

@Injectable()
export class ContaBancariaService extends BaseService<
  ContaBancariaDTO,
  ContaBancariaGridDTO
> {
  private static readonly CONTA_BANCARIA = 'conta-bancaria';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(ContaBancariaBackendMessages)
    );
  }

  getDomain(): string {
    return ContaBancariaService.CONTA_BANCARIA;
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

  protected override convertToDto(body: unknown): ContaBancariaDTO {
    return plainToInstance(
      ContaBancariaDTO,
      body as object
    ) as ContaBancariaDTO;
  }

  protected override convertToGrid(
    item: ContaBancariaGridDTO
  ): ContaBancariaGridDTO {
    return plainToInstance(
      ContaBancariaGridDTO,
      item as object
    ) as ContaBancariaGridDTO;
  }
}
