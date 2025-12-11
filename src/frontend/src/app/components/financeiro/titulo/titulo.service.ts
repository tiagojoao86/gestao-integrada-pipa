import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { TituloDTO } from './model/titulo-dto';
import { MessageService } from '../../base/messages/messages.service';
import { TituloBackendMessages } from './titulo-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class TituloService extends BaseService<TituloDTO> {
  private static readonly TITULO = 'titulo';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(TituloBackendMessages)
    );
  }

  getDominio(): string {
    return TituloService.TITULO;
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

  listarPessoasDisponiveis(): Observable<
    { id: string; nome: string; cpf?: string; cnpj?: string }[]
  > {
    return this.httpClient
      .get<{
        body: { id: string; nome: string; cpf?: string; cnpj?: string }[];
      }>(this.getUrl('/pessoas-disponiveis'))
      .pipe(
        map((response) => response.body),
        take(1)
      );
  }

  listarPlanosDisponiveis(
    unidadeNegocioId: string
  ): Observable<{ id: string; codigo: string; descricao: string }[]> {
    return this.httpClient
      .get<{ body: { id: string; codigo: string; descricao: string }[] }>(
        this.getUrl(`/planos-disponiveis?unidadeNegocioId=${unidadeNegocioId}`)
      )
      .pipe(
        map((response) => response.body),
        take(1)
      );
  }

  search(query: string, size = 10): Observable<TituloDTO[]> {
    const params = new URLSearchParams();
    if (query) params.set('q', query);
    params.set('size', String(size));
    // Endpoint moved to MovimentacaoFinanceiraController: /movimentacao-financeira/titulos/search
    const url = `/api/movimentacao-financeira/titulos/search?${params.toString()}`;
    return this.httpClient.get<{ body: TituloDTO[] }>(url).pipe(
      map((r) => r.body),
      take(1)
    );
  }
}
