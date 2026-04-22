import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { MessageService } from '../base/messages/messages.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DFCItemDTO {
  mes: string;
  entradas: number;
  saidas: number;
}

export type RegimeDFC = 'COMPETENCIA' | 'CAIXA';

export interface AtendimentoMesItemDTO {
  mes: string;
  total: number;
}

export interface SetorLookupItemDTO {
  id: string;
  nome: string;
}

export interface DFCDetalheItemDTO {
  mes: string;
  tipo: 'RECEITA' | 'DESPESA';
  agrupadorId: string;
  agrupadorNome: string;
  agrupadorCodigo: string;
  categoriaId: string;
  categoriaNome: string;
  categoriaCodigo: string;
  temAgrupador: boolean;
  total: number;
}

/**
 * Serviço de dashboards.
 * Não estende BaseService pois os endpoints de dashboard não seguem o padrão CRUD.
 * Cada quadro adicionado ao sistema gera um novo método nesta classe.
 */
@Injectable()
export class DashboardService {
  private static readonly DOMAIN = 'dashboard';

  protected httpClient = inject(HttpClient);
  protected messageService = inject(MessageService);

  getUrl(path = ''): string {
    return '/api/' + DashboardService.DOMAIN + path;
  }

  protected getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
    });
  }

  protected handleError(error: HttpErrorResponse): void {
    if (error.error?.messages?.length > 0) {
      this.messageService.erro(error.error.messages);
    } else {
      const msg =
        error.error?.title ||
        $localize`:@@erro.generico.inesperado:Ocorreu um erro inesperado.`;
      this.messageService.erro(msg);
    }
  }

  getFluxoCaixa(
    dataInicio: Date,
    dataFim: Date,
    regime: RegimeDFC
  ): Observable<DFCItemDTO[]> {
    const params = new HttpParams()
      .set('dataInicio', this.formatDate(dataInicio))
      .set('dataFim', this.formatDate(dataFim))
      .set('regime', regime);
    return this.httpClient.get<DFCItemDTO[]>(
      this.getUrl('/financeiro/fluxo-caixa'),
      { params }
    );
  }

  getFluxoCaixaDetalhe(
    dataInicio: Date,
    dataFim: Date,
    regime: RegimeDFC
  ): Observable<DFCDetalheItemDTO[]> {
    const params = new HttpParams()
      .set('dataInicio', this.formatDate(dataInicio))
      .set('dataFim', this.formatDate(dataFim))
      .set('regime', regime);
    return this.httpClient.get<DFCDetalheItemDTO[]>(
      this.getUrl('/financeiro/fluxo-caixa-detalhe'),
      { params }
    );
  }

  getSetoresByUnidades(unidadeIds: string[] = []): Observable<SetorLookupItemDTO[]> {
    let params = new HttpParams();
    unidadeIds.forEach((id) => (params = params.append('unidadeIds', id)));
    return this.httpClient.get<SetorLookupItemDTO[]>(
      this.getUrl('/atendimento/setores'),
      { params }
    );
  }

  getAtendimentosPorMes(
    dataInicio: Date,
    dataFim: Date,
    unidadeIds: string[] = [],
    setorIds: string[] = []
  ): Observable<AtendimentoMesItemDTO[]> {
    let params = new HttpParams()
      .set('dataInicio', this.formatDate(dataInicio))
      .set('dataFim', this.formatDate(dataFim));
    unidadeIds.forEach((id) => (params = params.append('unidadeIds', id)));
    setorIds.forEach((id) => (params = params.append('setorIds', id)));
    return this.httpClient.get<AtendimentoMesItemDTO[]>(
      this.getUrl('/atendimento/por-mes'),
      { params }
    );
  }

  private formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}
