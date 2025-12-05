import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ContaBancariaDTO } from './model/conta-bancaria-dto';
import { MessageService } from '../../base/messages/messages.service';
import { ContaBancariaBackendMessages } from './conta-bancaria-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class ContaBancariaService extends BaseService<ContaBancariaDTO> {
  private static readonly CONTA_BANCARIA = 'conta-bancaria';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(ContaBancariaBackendMessages)
    );
  }

  getDominio(): string {
    return ContaBancariaService.CONTA_BANCARIA;
  }
}
