import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { TabelaDTO } from './model/tabela-dto';
import { TabelaGridDTO } from './model/tabela-grid-dto';

@Injectable()
export class TabelaService extends BaseService<TabelaDTO, TabelaGridDTO> {
  private static readonly DOMAIN = 'tabela';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return TabelaService.DOMAIN;
  }

  protected override convertToDto(body: unknown): TabelaDTO {
    return plainToInstance(TabelaDTO, body as object) as TabelaDTO;
  }

  protected override convertToGrid(item: TabelaGridDTO): TabelaGridDTO {
    return plainToInstance(TabelaGridDTO, item as object) as TabelaGridDTO;
  }
}
