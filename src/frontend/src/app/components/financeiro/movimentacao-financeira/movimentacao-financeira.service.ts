import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MovimentacaoFinanceiraDTO } from './model/movimentacao-financeira.dto';
import { BaseService } from '../../base/base-service';
import { TituloDTO } from '../titulo/model/titulo-dto';
import { HttpClient } from '@angular/common/http';
import { MessageService } from '../../base/messages/messages.service';
import { MovimentacaoFinanceiraBackendMessages } from './movimentacao-financeira-backend-message.service';

@Injectable({ providedIn: 'root' })
export class MovimentacaoFinanceiraService extends BaseService<MovimentacaoFinanceiraDTO> {
  private static readonly MOVIMENTACAO_FINANCEIRA = 'movimentacao-financeira';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(MovimentacaoFinanceiraBackendMessages)
    );
  }

  getDominio(): string {
    return MovimentacaoFinanceiraService.MOVIMENTACAO_FINANCEIRA;
  }

  listarTitulos(): Observable<TituloDTO[]> {
    return this.httpClient.get<TituloDTO[]>(`${this.getUrl('/titulos')}`);
  }
}
