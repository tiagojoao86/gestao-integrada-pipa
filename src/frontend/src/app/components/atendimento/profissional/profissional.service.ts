import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { instanceToPlain, plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { ProfissionalDTO } from './model/profissional-dto';
import { ProfissionalGridDTO } from './model/profissional-grid-dto';

@Injectable()
export class ProfissionalService extends BaseService<ProfissionalDTO, ProfissionalGridDTO> {
  private static readonly DOMAIN = 'profissional';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return ProfissionalService.DOMAIN;
  }

  protected override convertToDto(body: unknown): ProfissionalDTO {
    return plainToInstance(ProfissionalDTO, body as object) as ProfissionalDTO;
  }

  protected override convertToGrid(item: ProfissionalGridDTO): ProfissionalGridDTO {
    return plainToInstance(ProfissionalGridDTO, item as object) as ProfissionalGridDTO;
  }

  protected override convertToPlain(item: ProfissionalDTO): ProfissionalDTO {
    return instanceToPlain(item, {
      enableCircularCheck: true,
      exposeDefaultValues: true,
    }) as ProfissionalDTO;
  }
}
