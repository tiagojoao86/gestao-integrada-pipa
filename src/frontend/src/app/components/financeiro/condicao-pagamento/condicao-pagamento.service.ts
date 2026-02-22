import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CondicaoPagamentoDTO } from './model/condicao-pagamento.dto';
import { CondicaoPagamentoGridDTO } from './model/condicao-pagamento-grid.dto';
import { instanceToPlain, plainToInstance } from 'class-transformer';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class CondicaoPagamentoService extends BaseService<
  CondicaoPagamentoDTO,
  CondicaoPagamentoGridDTO
> {
  private static readonly DOMINIO = 'condicao-pagamento';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  protected override convertToDto(body: unknown): CondicaoPagamentoDTO {
    return plainToInstance(
      CondicaoPagamentoDTO,
      body as object
    ) as CondicaoPagamentoDTO;
  }

  protected override convertToGrid(
    item: CondicaoPagamentoGridDTO
  ): CondicaoPagamentoGridDTO {
    return plainToInstance(
      CondicaoPagamentoGridDTO,
      item as object
    ) as CondicaoPagamentoGridDTO;
  }

  protected override convertToPlain(
    item: CondicaoPagamentoDTO
  ): CondicaoPagamentoDTO {
    const instance = CondicaoPagamentoDTO.from(item);
    return instanceToPlain(instance, {
      enableCircularCheck: true,
      exposeDefaultValues: true,
    }) as CondicaoPagamentoDTO;
  }

  getDomain(): string {
    return CondicaoPagamentoService.DOMINIO;
  }
}
