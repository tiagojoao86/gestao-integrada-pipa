import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UsuarioDTO } from './model/usuario-dto';
import { MessageService } from '../../base/messages/messages.service';
import { UsuarioBackendMessages } from './usuario-backend-message.service';
import { BaseService } from '../../base/base-service';

@Injectable()
export class UsuarioService extends BaseService<UsuarioDTO> {
  private static readonly USUARIO = 'usuario';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(UsuarioBackendMessages)
    );
  }

  getDominio(): string {
    return UsuarioService.USUARIO;
  }
}
