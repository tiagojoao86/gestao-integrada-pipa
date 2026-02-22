import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { PlanoContasDTO } from './model/plano-contas-dto';
import { PlanoContasGridDTO } from './model/plano-contas-grid-dto';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class PlanoContasService extends BaseService<
  PlanoContasDTO,
  PlanoContasGridDTO
> {
  private static readonly PLANO_CONTAS = 'plano-contas';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return PlanoContasService.PLANO_CONTAS;
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
