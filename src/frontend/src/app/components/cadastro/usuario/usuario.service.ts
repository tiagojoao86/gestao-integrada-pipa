import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { UsuarioDTO } from './model/usuario-dto';
import { UnidadeNegocioDTO } from '../unidade-negocio/model/unidade-negocio-dto';
import { PerfilParaVinculoDTO } from '../perfil/model/perfil-para-vinculo-dto';
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

  listarUnidadesDisponiveis(): Observable<UnidadeNegocioDTO[]> {
    return this.httpClient
      .get<{ body: UnidadeNegocioDTO[] }>(this.getUrl('/unidades-disponiveis'))
      .pipe(
        map((response) => response.body),
        take(1)
      );
  }

  listarPerfisDisponiveis(): Observable<PerfilParaVinculoDTO[]> {
    return this.httpClient
      .get<{ body: PerfilParaVinculoDTO[] }>(this.getUrl('/perfis-disponiveis'))
      .pipe(
        map((response) => response.body),
        take(1)
      );
  }
}
