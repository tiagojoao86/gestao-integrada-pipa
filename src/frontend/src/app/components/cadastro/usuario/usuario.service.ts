import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, take } from 'rxjs';
import { map } from 'rxjs/operators';
import { UsuarioDTO } from './model/usuario-dto';
import { UnidadeNegocioDTO } from '../unidade-negocio/model/unidade-negocio-dto';
import { PerfilParaVinculoDTO } from '../perfil/model/perfil-para-vinculo-dto';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { UsuarioGridDTO } from './model/usuario-grid-dto';
import { CaixaGridDTO } from '../../financeiro/caixa/model/caixa-grid-dto';
import { plainToInstance } from 'class-transformer';

@Injectable()
export class UsuarioService extends BaseService<UsuarioDTO, UsuarioGridDTO> {
  private static readonly USUARIO = 'usuario';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
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

  listarCaixasDisponiveis(): Observable<CaixaGridDTO[]> {
    return this.httpClient.get<CaixaGridDTO[]>(`${this.urlBase}caixa/todos-ativos`).pipe(take(1));
  }

  listarCaixasDoUsuario(usuarioId: string): Observable<string[]> {
    return this.httpClient
      .get<string[]>(`${this.urlBase}caixa/por-usuario/${usuarioId}`)
      .pipe(take(1));
  }

  atualizarCaixasDoUsuario(usuarioId: string, caixaIds: string[]): Observable<void> {
    return this.httpClient
      .put<void>(`${this.urlBase}caixa/por-usuario/${usuarioId}`, caixaIds)
      .pipe(take(1));
  }

  protected override convertToDto(body: unknown): UsuarioDTO {
    return plainToInstance(UsuarioDTO, body as object) as UsuarioDTO;
  }

  protected override convertToGrid(item: UsuarioGridDTO): UsuarioGridDTO {
    return plainToInstance(UsuarioGridDTO, item as object) as UsuarioGridDTO;
  }
}
