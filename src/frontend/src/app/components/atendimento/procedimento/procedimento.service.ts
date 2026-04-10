import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { instanceToPlain, plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { ProcedimentoDTO } from './model/procedimento-dto';
import { ProcedimentoGridDTO } from './model/procedimento-grid-dto';

@Injectable()
export class ProcedimentoService extends BaseService<ProcedimentoDTO, ProcedimentoGridDTO> {
  private static readonly DOMAIN = 'procedimento';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return ProcedimentoService.DOMAIN;
  }

  protected override convertToDto(body: unknown): ProcedimentoDTO {
    return plainToInstance(ProcedimentoDTO, body as object) as ProcedimentoDTO;
  }

  protected override convertToGrid(item: ProcedimentoGridDTO): ProcedimentoGridDTO {
    return plainToInstance(ProcedimentoGridDTO, item as object) as ProcedimentoGridDTO;
  }

  protected override convertToPlain(item: ProcedimentoDTO): ProcedimentoDTO {
    return instanceToPlain(item, {
      enableCircularCheck: true,
      exposeDefaultValues: true,
    }) as ProcedimentoDTO;
  }
}
