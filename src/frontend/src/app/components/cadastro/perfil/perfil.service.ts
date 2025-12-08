import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PerfilDTO } from './model/perfil-dto';
import { MessageService } from '../../base/messages/messages.service';
import { PerfilBackendMessages } from './perfil-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class PerfilService extends BaseService<PerfilDTO> {
  private static readonly PERFIL = 'perfil';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(PerfilBackendMessages)
    );
  }

  getDominio(): string {
    return PerfilService.PERFIL;
  }
}
