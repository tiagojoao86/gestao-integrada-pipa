import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AberturaCaixaDTO } from './model/abertura-caixa-dto';
import { CaixaComStatusDTO } from './model/caixa-com-status-dto';
import { MovimentacaoCaixaDTO } from './model/movimentacao-caixa-dto';
import { LancamentoFinanceiroGridDTO } from '../../atendimento/lancamento/model/lancamento-financeiro-grid-dto';

@Injectable()
export class AberturaCaixaService {
  private readonly baseUrl = '/api/abertura-caixa';
  private http = inject(HttpClient);

  abrir(caixaId: string, valorAbertura: number): Observable<AberturaCaixaDTO> {
    return this.http.post<AberturaCaixaDTO>(`${this.baseUrl}/abrir`, {
      caixaId,
      valorAbertura,
    });
  }

  fechar(id: string, valorConferencia: number, observacoes: string): Observable<AberturaCaixaDTO> {
    return this.http.post<AberturaCaixaDTO>(`${this.baseUrl}/${id}/fechar`, {
      valorConferencia,
      observacoes,
    });
  }

  listarMeusCaixas(): Observable<CaixaComStatusDTO[]> {
    return this.http.get<CaixaComStatusDTO[]>(`${this.baseUrl}/meus-caixas`);
  }

  statusPorCaixa(caixaId: string): Observable<CaixaComStatusDTO> {
    return this.http.get<CaixaComStatusDTO>(`${this.baseUrl}/status/${caixaId}`);
  }

  listarLancamentosPendentes(aberturaCaixaId: string): Observable<LancamentoFinanceiroGridDTO[]> {
    return this.http.get<LancamentoFinanceiroGridDTO[]>(
      `${this.baseUrl}/${aberturaCaixaId}/lancamentos-pendentes`
    );
  }

  listarMovimentacoes(aberturaCaixaId: string): Observable<MovimentacaoCaixaDTO[]> {
    return this.http.get<MovimentacaoCaixaDTO[]>(
      `${this.baseUrl}/${aberturaCaixaId}/movimentacoes`
    );
  }
}
