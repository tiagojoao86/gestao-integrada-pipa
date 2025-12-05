import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PessoaDTO } from './model/pessoa-dto';
import { MessageService } from '../../base/messages/messages.service';
import { PessoaBackendMessages } from './pessoa-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class PessoaService extends BaseService<PessoaDTO> {
  private static readonly PESSOA = 'pessoa';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(PessoaBackendMessages)
    );
  }

  getDominio(): string {
    return PessoaService.PESSOA;
  }
}
