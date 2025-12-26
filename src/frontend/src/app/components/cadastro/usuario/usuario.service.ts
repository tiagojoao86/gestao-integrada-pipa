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
import { UsuarioGridDTO } from './model/usuario-grid-dto';
import { plainToInstance } from 'class-transformer';
import { AuditInfoData } from '../../base/audit-info/audit-info.component';
import { Response } from '../../base/model/response';

@Injectable()
export class UsuarioService extends BaseService<UsuarioDTO, UsuarioGridDTO> {
  private static readonly USUARIO = 'usuario';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(UsuarioBackendMessages)
    );
  }

  getDomain(): string {
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

  getAuditInfo(id: string): Observable<Response<AuditInfoData>> {
    return this.httpClient
      .get<Response<AuditInfoData>>(this.getUrl(`/${id}/audit-info`))
      .pipe(take(1));
  }

  protected override convertToDto(body: unknown): UsuarioDTO {
    return plainToInstance(UsuarioDTO, body as object) as UsuarioDTO;
  }

  protected override convertToGrid(item: UsuarioGridDTO): UsuarioGridDTO {
    return plainToInstance(UsuarioGridDTO, item as object) as UsuarioGridDTO;
  }
}
