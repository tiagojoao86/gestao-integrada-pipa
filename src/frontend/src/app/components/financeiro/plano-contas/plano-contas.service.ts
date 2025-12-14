import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { PlanoContasDTO } from './model/plano-contas-dto';
import { MessageService } from '../../base/messages/messages.service';
import { PlanoContasBackendMessages } from './plano-contas-backend-message.service';
import { BaseService } from '../../base/base-service';
import { PageRequest } from '../../base/model/page-request';
import { FilterLogicOperator } from '../../base/model/filter-dto';

@Injectable()
export class PlanoContasService extends BaseService<PlanoContasDTO> {
  private static readonly PLANO_CONTAS = 'plano-contas';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(PlanoContasBackendMessages)
    );
  }

  getDomain(): string {
    return PlanoContasService.PLANO_CONTAS;
  }

  listarParaVinculo(): Observable<
    { id: string; codigo: string; descricao: string; displayLabel: string }[]
  > {
    const request = new PageRequest(
      { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
      1000,
      0,
      []
    );
    return this.list(request).pipe(
      map((response) =>
        response.body.content.map((pc: PlanoContasDTO) => ({
          id: pc.id!,
          codigo: pc.codigo,
          descricao: pc.descricao,
          displayLabel: `${pc.codigo} - ${pc.descricao}`,
        }))
      )
    );
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
