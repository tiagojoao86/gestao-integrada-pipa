import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PlanoContasDTO } from './model/plano-contas-dto';
import { MessageService } from '../../base/messages/messages.service';
import { PlanoContasBackendMessages } from './plano-contas-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class PlanoContasService extends BaseService<PlanoContasDTO> {
  private static readonly PLANO_CONTAS = 'plano-contas';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(PlanoContasBackendMessages)
    );
  }

  getDominio(): string {
    return PlanoContasService.PLANO_CONTAS;
  }
}
