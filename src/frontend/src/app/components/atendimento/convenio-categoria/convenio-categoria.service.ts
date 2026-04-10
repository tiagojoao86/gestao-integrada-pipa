import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { instanceToPlain, plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { ConvenioCategoriaDTO } from './model/convenio-categoria-dto';
import { ConvenioCategoriaGridDTO } from './model/convenio-categoria-grid-dto';

@Injectable()
export class ConvenioCategoriaService extends BaseService<ConvenioCategoriaDTO, ConvenioCategoriaGridDTO> {
  private static readonly DOMAIN = 'convenio-categoria';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return ConvenioCategoriaService.DOMAIN;
  }

  protected override convertToDto(body: unknown): ConvenioCategoriaDTO {
    return plainToInstance(ConvenioCategoriaDTO, body as object) as ConvenioCategoriaDTO;
  }

  protected override convertToGrid(item: ConvenioCategoriaGridDTO): ConvenioCategoriaGridDTO {
    return plainToInstance(ConvenioCategoriaGridDTO, item as object) as ConvenioCategoriaGridDTO;
  }

  protected override convertToPlain(item: ConvenioCategoriaDTO): ConvenioCategoriaDTO {
    return instanceToPlain(item, {
      enableCircularCheck: true,
      exposeDefaultValues: true,
    }) as ConvenioCategoriaDTO;
  }
}
