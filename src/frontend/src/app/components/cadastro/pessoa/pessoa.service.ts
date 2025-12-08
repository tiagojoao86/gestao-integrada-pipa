import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PessoaDTO } from './model/pessoa-dto';
import { MessageService } from '../../base/messages/messages.service';
import { PessoaBackendMessages } from './pessoa-backend-message.service';
import { BaseService } from '../../base/base-service';
import { PageRequest } from '../../base/model/page-request';
import { FilterLogicOperator } from '../../base/model/filter-dto';

@Injectable()
export class PessoaService extends BaseService<PessoaDTO> {
  private static readonly PESSOA = 'pessoa';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(PessoaBackendMessages)
    );
  }

  getDominio(): string {
    return PessoaService.PESSOA;
  }

  listarParaVinculo(): Observable<any[]> {
    const request = new PageRequest(
      { filterLogicOperator: FilterLogicOperator.AND.getKey(), items: [] },
      1000,
      0,
      []
    );
    return this.list(request).pipe(
      map((response) =>
        response.body.content.map((p: any) => ({
          id: p.id,
          nome: p.nome,
        }))
      )
    );
  }
}
