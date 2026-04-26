import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../../base/messages/messages.service';
import { BaseService } from '../../../base/base-service';
import { AgendaRegraDTO } from './model/agenda-regra-dto';
import { AgendaRegraGridDTO } from './model/agenda-regra-grid-dto';
import { Response } from '../../../base/model/response';

export interface ConflitoPar {
  regraIdA: string;
  regraIdB: string;
}

@Injectable()
export class AgendaRegraService extends BaseService<AgendaRegraDTO, AgendaRegraGridDTO> {
  private static readonly DOMINIO = 'agendamento/regra';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  protected override convertToDto(body: unknown): AgendaRegraDTO {
    return plainToInstance(AgendaRegraDTO, body as object) as AgendaRegraDTO;
  }

  protected override convertToGrid(item: AgendaRegraGridDTO): AgendaRegraGridDTO {
    return plainToInstance(AgendaRegraGridDTO, item as object) as AgendaRegraGridDTO;
  }

  getDomain(): string {
    return AgendaRegraService.DOMINIO;
  }

  listByAgenda(agendaId: string): Observable<Response<AgendaRegraGridDTO[]>> {
    return this.httpClient.get<Response<AgendaRegraGridDTO[]>>(
      this.getUrl('/by-agenda'),
      { headers: this.getHeaders(), params: { agendaId } }
    );
  }

  getConflitos(agendaId: string): Observable<Response<ConflitoPar[]>> {
    return this.httpClient.get<Response<ConflitoPar[]>>(
      this.getUrl('/conflitos'),
      { headers: this.getHeaders(), params: { agendaId } }
    );
  }
}
