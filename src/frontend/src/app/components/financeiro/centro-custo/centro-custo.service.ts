import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CentroCustoDTO } from './model/centro-custo-dto';
import { CentroCustoGridDTO } from './model/centro-custo-grid-dto';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { plainToInstance } from 'class-transformer';

@Injectable()
export class CentroCustoService extends BaseService<
  CentroCustoDTO,
  CentroCustoGridDTO
> {
  private static readonly DOMAIN = 'centro-custo';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return CentroCustoService.DOMAIN;
  }

  protected override convertToDto(body: unknown): CentroCustoDTO {
    return plainToInstance(CentroCustoDTO, body as object) as CentroCustoDTO;
  }

  protected override convertToGrid(
    item: CentroCustoGridDTO
  ): CentroCustoGridDTO {
    return plainToInstance(
      CentroCustoGridDTO,
      item as object
    ) as CentroCustoGridDTO;
  }
}
