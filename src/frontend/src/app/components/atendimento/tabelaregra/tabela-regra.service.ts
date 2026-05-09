import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { Response } from '../../base/model/response';
import { TabelaRegraDTO } from './model/tabela-regra-dto';
import { TabelaRegraGridDTO } from './model/tabela-regra-grid-dto';
import { ResolverProcedimentoResponse } from '../lancamento/model/resolver-procedimento-response';

@Injectable()
export class TabelaRegraService extends BaseService<TabelaRegraDTO, TabelaRegraGridDTO> {
  private static readonly DOMAIN = 'tabela-regra';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return TabelaRegraService.DOMAIN;
  }

  protected override convertToDto(body: unknown): TabelaRegraDTO {
    return plainToInstance(TabelaRegraDTO, body as object) as TabelaRegraDTO;
  }

  protected override convertToGrid(item: TabelaRegraGridDTO): TabelaRegraGridDTO {
    return plainToInstance(TabelaRegraGridDTO, item as object) as TabelaRegraGridDTO;
  }

  resolverProcedimento(
    convenioId: string,
    convenioCategoriaId: string | null | undefined,
    procedimentoId: string,
    dataReferencia: string
  ): Observable<Response<ResolverProcedimentoResponse>> {
    const params: Record<string, string> = { convenioId, procedimentoId, dataReferencia };
    if (convenioCategoriaId) params['convenioCategoriaId'] = convenioCategoriaId;
    return this.httpClient.get<Response<ResolverProcedimentoResponse>>(
      this.getUrl('/resolver-procedimento'),
      { params, headers: this.getHeaders() }
    );
  }
}
