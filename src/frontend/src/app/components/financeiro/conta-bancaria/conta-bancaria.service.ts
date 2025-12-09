import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { ContaBancariaDTO } from './model/conta-bancaria-dto';
import { MessageService } from '../../base/messages/messages.service';
import { ContaBancariaBackendMessages } from './conta-bancaria-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class ContaBancariaService extends BaseService<ContaBancariaDTO> {
  private static readonly CONTA_BANCARIA = 'conta-bancaria';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(ContaBancariaBackendMessages)
    );
  }

  getDominio(): string {
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
}
