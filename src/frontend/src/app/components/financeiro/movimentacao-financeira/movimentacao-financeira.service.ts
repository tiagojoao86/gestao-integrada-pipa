import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MovimentacaoFinanceiraDTO } from './model/movimentacao-financeira.dto';
import { MovimentacaoFinanceiraGridDTO } from './model/movimentacao-financeira-grid.dto';
import { BaseService } from '../../base/base-service';
import { TituloDTO } from '../titulo/model/titulo-dto';
import { HttpClient } from '@angular/common/http';
import { MessageService } from '../../base/messages/messages.service';
import { plainToInstance } from 'class-transformer';

@Injectable({ providedIn: 'root' })
export class MovimentacaoFinanceiraService extends BaseService<
  MovimentacaoFinanceiraDTO,
  MovimentacaoFinanceiraGridDTO
> {
  private static readonly MOVIMENTACAO_FINANCEIRA = 'movimentacao-financeira';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return MovimentacaoFinanceiraService.MOVIMENTACAO_FINANCEIRA;
  }

  listarTitulos(): Observable<TituloDTO[]> {
    return this.httpClient.get<TituloDTO[]>(`${this.getUrl('/titulos')}`);
  }

  protected override convertToDto(body: unknown): MovimentacaoFinanceiraDTO {
    return plainToInstance(
      MovimentacaoFinanceiraDTO,
      body as object
    ) as MovimentacaoFinanceiraDTO;
  }

  protected override convertToGrid(
    item: MovimentacaoFinanceiraGridDTO
  ): MovimentacaoFinanceiraGridDTO {
    return plainToInstance(
      MovimentacaoFinanceiraGridDTO,
      item as object
    ) as MovimentacaoFinanceiraGridDTO;
  }
}
