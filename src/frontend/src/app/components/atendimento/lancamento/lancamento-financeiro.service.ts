import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { Response } from '../../base/model/response';
import { LancamentoFinanceiroDTO } from './model/lancamento-financeiro-dto';
import { LancamentoFinanceiroGridDTO } from './model/lancamento-financeiro-grid-dto';
import { LancamentoFinanceiroProcedimentoDTO } from './model/lancamento-financeiro-procedimento-dto';
import { ResolverProcedimentoResponse } from './model/resolver-procedimento-response';

@Injectable()
export class LancamentoFinanceiroService
  extends BaseService<LancamentoFinanceiroDTO, LancamentoFinanceiroGridDTO> {

  private static readonly DOMAIN = 'lancamento-financeiro';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return LancamentoFinanceiroService.DOMAIN;
  }

  protected override convertToDto(body: unknown): LancamentoFinanceiroDTO {
    const dto = plainToInstance(LancamentoFinanceiroDTO, body as object) as LancamentoFinanceiroDTO;
    const raw = body as { procedimentos?: unknown[] };
    if (raw?.procedimentos) {
      dto.procedimentos = raw.procedimentos.map(
        (p) => plainToInstance(
          LancamentoFinanceiroProcedimentoDTO, p as object) as LancamentoFinanceiroProcedimentoDTO
      );
    }
    return dto;
  }

  protected override convertToGrid(item: LancamentoFinanceiroGridDTO): LancamentoFinanceiroGridDTO {
    return plainToInstance(LancamentoFinanceiroGridDTO, item as object) as LancamentoFinanceiroGridDTO;
  }

  fecharParaPagamento(id: string): Observable<Response<LancamentoFinanceiroDTO>> {
    return this.httpClient.post<Response<LancamentoFinanceiroDTO>>(
      `${this.urlBase}${this.getDomain()}/${id}/fechar-pagamento`, {}
    );
  }

  fecharParaFaturamento(id: string): Observable<Response<LancamentoFinanceiroDTO>> {
    return this.httpClient.post<Response<LancamentoFinanceiroDTO>>(
      `${this.urlBase}${this.getDomain()}/${id}/fechar-faturamento`, {}
    );
  }

  cancelar(id: string): Observable<Response<LancamentoFinanceiroDTO>> {
    return this.httpClient.post<Response<LancamentoFinanceiroDTO>>(
      `${this.urlBase}${this.getDomain()}/${id}/cancelar`, {}
    );
  }

  receber(
    id: string,
    request: { aberturaCaixaId: string; valorRecebido: number; formaPagamento: string; observacoes?: string }
  ): Observable<Response<LancamentoFinanceiroDTO>> {
    return this.httpClient.post<Response<LancamentoFinanceiroDTO>>(
      `${this.urlBase}${this.getDomain()}/${id}/receber`, request
    );
  }

  resolverProcedimento(
    lancamentoId: string,
    procedimentoId: string
  ): Observable<Response<ResolverProcedimentoResponse>> {
    return this.httpClient.get<Response<ResolverProcedimentoResponse>>(
      `${this.urlBase}${this.getDomain()}/${lancamentoId}/resolver-procedimento`,
      { params: { procedimentoId }, headers: this.getHeaders() }
    );
  }
}
