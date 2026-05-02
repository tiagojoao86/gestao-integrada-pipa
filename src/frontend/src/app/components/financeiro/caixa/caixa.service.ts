import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { plainToInstance } from 'class-transformer';
import { CaixaDTO } from './model/caixa-dto';
import { CaixaGridDTO } from './model/caixa-grid-dto';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class CaixaService extends BaseService<CaixaDTO, CaixaGridDTO> {
  private static readonly CAIXA = 'caixa';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return CaixaService.CAIXA;
  }

  protected override convertToDto(body: CaixaDTO): CaixaDTO {
    return plainToInstance(CaixaDTO, body as object) as CaixaDTO;
  }

  protected override convertToGrid(item: CaixaGridDTO): CaixaGridDTO {
    return plainToInstance(CaixaGridDTO, item as object) as CaixaGridDTO;
  }
}
