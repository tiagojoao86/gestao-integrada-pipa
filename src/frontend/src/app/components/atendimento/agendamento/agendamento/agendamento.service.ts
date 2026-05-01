import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../../base/messages/messages.service';
import { BaseService } from '../../../base/base-service';
import { AgendamentoDTO } from './model/agendamento-dto';
import { AgendamentoGridDTO } from './model/agendamento-grid-dto';
import { SlotDTO } from './model/slot-dto';
import { Response } from '../../../base/model/response';

@Injectable()
export class AgendamentoService extends BaseService<AgendamentoDTO, AgendamentoGridDTO> {
  private static readonly DOMINIO = 'agendamento/agendamento';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  protected override convertToDto(body: unknown): AgendamentoDTO {
    return plainToInstance(AgendamentoDTO, body as object) as AgendamentoDTO;
  }

  protected override convertToGrid(item: AgendamentoGridDTO): AgendamentoGridDTO {
    return plainToInstance(AgendamentoGridDTO, item as object) as AgendamentoGridDTO;
  }

  getDomain(): string {
    return AgendamentoService.DOMINIO;
  }

  listarSlots(agendaId: string, dataInicio: string, dataFim: string): Observable<Response<SlotDTO[]>> {
    return this.httpClient.get<Response<SlotDTO[]>>(
      this.getUrl('/slots'),
      { headers: this.getHeaders(), params: { agendaId, dataInicio, dataFim } }
    );
  }

  conflitoPaciente(
    pacienteId: string, dataInicio: string, dataFim: string
  ): Observable<Response<AgendamentoDTO[]>> {
    return this.httpClient.get<Response<AgendamentoDTO[]>>(
      this.getUrl('/conflito-paciente'),
      { headers: this.getHeaders(), params: { pacienteId, dataInicio, dataFim } }
    );
  }

  listarPorPaciente(
    pessoaId: string, dataInicio: string, dataFim: string
  ): Observable<Response<AgendamentoGridDTO[]>> {
    return this.httpClient.get<Response<AgendamentoGridDTO[]>>(
      this.getUrl('/visao-paciente'),
      { headers: this.getHeaders(), params: { pessoaId, dataInicio, dataFim } }
    );
  }

  vincularAtendimento(agendamentoId: string, atendimentoId: string): Observable<Response<AgendamentoDTO>> {
    return this.httpClient.patch<Response<AgendamentoDTO>>(
      this.getUrl(`/${agendamentoId}/vincular-atendimento/${atendimentoId}`),
      {},
      { headers: this.getHeaders() }
    );
  }

  cancelar(id: string): Observable<Response<AgendamentoDTO>> {
    return this.httpClient.patch<Response<AgendamentoDTO>>(
      this.getUrl('/cancelar/' + id),
      {},
      { headers: this.getHeaders() }
    );
  }

  realizar(id: string): Observable<Response<AgendamentoDTO>> {
    return this.httpClient.patch<Response<AgendamentoDTO>>(
      this.getUrl('/realizar/' + id),
      {},
      { headers: this.getHeaders() }
    );
  }
}
