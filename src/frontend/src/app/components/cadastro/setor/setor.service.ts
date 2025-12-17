import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SetorDTO } from './model/setor-dto';
import { SetorGridDTO } from './model/setor-grid-dto';
import { MessageService } from '../../base/messages/messages.service';
import { SetorBackendMessageService } from './setor-backend-message.service';
import { BaseService } from '../../base/base-service';
import { plainToInstance } from 'class-transformer';

@Injectable()
export class SetorService extends BaseService<SetorDTO, SetorGridDTO> {
  private static readonly DOMAIN = 'setor';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(SetorBackendMessageService)
    );
  }

  getDomain(): string {
    return SetorService.DOMAIN;
  }

  protected override convertToDto(body: unknown): SetorDTO {
    return plainToInstance(SetorDTO, body as object) as SetorDTO;
  }

  protected override convertToGrid(item: SetorGridDTO): SetorGridDTO {
    return plainToInstance(SetorGridDTO, item as object) as SetorGridDTO;
  }
}
