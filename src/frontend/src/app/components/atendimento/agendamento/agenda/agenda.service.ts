import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../../base/messages/messages.service';
import { BaseService } from '../../../base/base-service';
import { AgendaDTO } from './model/agenda-dto';
import { AgendaGridDTO } from './model/agenda-grid-dto';

@Injectable()
export class AgendaService extends BaseService<AgendaDTO, AgendaGridDTO> {
  private static readonly DOMINIO = 'agenda';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  protected override convertToDto(body: unknown): AgendaDTO {
    return plainToInstance(AgendaDTO, body as object) as AgendaDTO;
  }

  protected override convertToGrid(item: AgendaGridDTO): AgendaGridDTO {
    return plainToInstance(AgendaGridDTO, item as object) as AgendaGridDTO;
  }

  getDomain(): string {
    return AgendaService.DOMINIO;
  }
}
