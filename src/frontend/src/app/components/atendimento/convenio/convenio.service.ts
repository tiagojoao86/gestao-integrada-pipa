import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { instanceToPlain, plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { ConvenioDTO } from './model/convenio-dto';
import { ConvenioGridDTO } from './model/convenio-grid-dto';

@Injectable()
export class ConvenioService extends BaseService<ConvenioDTO, ConvenioGridDTO> {
  private static readonly DOMAIN = 'convenio';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return ConvenioService.DOMAIN;
  }

  protected override convertToDto(body: unknown): ConvenioDTO {
    return plainToInstance(ConvenioDTO, body as object) as ConvenioDTO;
  }

  protected override convertToGrid(item: ConvenioGridDTO): ConvenioGridDTO {
    return plainToInstance(ConvenioGridDTO, item as object) as ConvenioGridDTO;
  }

  protected override convertToPlain(item: ConvenioDTO): ConvenioDTO {
    return instanceToPlain(item, {
      enableCircularCheck: true,
      exposeDefaultValues: true,
    }) as ConvenioDTO;
  }
}
