import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PerfilDTO } from './model/perfil-dto';
import { MessageService } from '../../base/messages/messages.service';
import { BaseService } from '../../base/base-service';
import { PerfilGridDTO } from './model/perfil-grid-dto';
import { plainToInstance } from 'class-transformer';
@Injectable()
export class PerfilService extends BaseService<PerfilDTO, PerfilGridDTO> {
  private static readonly PERFIL = 'perfil';

  constructor() {
    super(inject(HttpClient), inject(MessageService));
  }

  getDomain(): string {
    return PerfilService.PERFIL;
  }

  protected override convertToDto(body: unknown): PerfilDTO {
    return plainToInstance(PerfilDTO, body as object) as PerfilDTO;
  }

  protected override convertToGrid(item: PerfilGridDTO): PerfilGridDTO {
    return plainToInstance(PerfilGridDTO, item as object) as PerfilGridDTO;
  }
}
