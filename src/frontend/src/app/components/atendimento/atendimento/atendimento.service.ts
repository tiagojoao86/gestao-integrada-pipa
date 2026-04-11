import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { AtendimentoDTO } from './model/atendimento-dto';
import { AtendimentoGridDTO } from './model/atendimento-grid-dto';
import { AtendimentoProcedimentoDTO } from './model/atendimento-procedimento-dto';

@Injectable()
export class AtendimentoService extends BaseService<AtendimentoDTO, AtendimentoGridDTO> {
  private static readonly DOMAIN = 'atendimento';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return AtendimentoService.DOMAIN;
  }

  protected override convertToDto(body: unknown): AtendimentoDTO {
    const dto = plainToInstance(AtendimentoDTO, body as object) as AtendimentoDTO;
    const raw = body as { procedimentos?: unknown[] };
    if (raw?.procedimentos) {
      dto.procedimentos = raw.procedimentos.map(
        (p) => plainToInstance(AtendimentoProcedimentoDTO, p as object) as AtendimentoProcedimentoDTO
      );
    }
    return dto;
  }

  protected override convertToGrid(item: AtendimentoGridDTO): AtendimentoGridDTO {
    return plainToInstance(AtendimentoGridDTO, item as object) as AtendimentoGridDTO;
  }
}
