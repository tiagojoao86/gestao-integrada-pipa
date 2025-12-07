import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PerfilDTO } from './model/perfil-dto';
import { PerfilParaVinculoDTO } from './model/perfil-para-vinculo-dto';
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

  listarParaVinculo(): Observable<PerfilParaVinculoDTO[]> {
    return this.httpClient
      .get<{ body: PerfilParaVinculoDTO[] }>(
        `${this.getUrl().replace('perfil', 'usuario')}/perfis-disponiveis`
      )
      .pipe(map((response) => response.body));
  }
}
