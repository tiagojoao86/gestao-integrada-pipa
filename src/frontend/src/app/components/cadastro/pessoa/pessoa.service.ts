import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PessoaDTO } from './model/pessoa-dto';
import { PessoaGridDTO } from './model/pessoa-grid-dto';
import { MessageService } from '../../base/messages/messages.service';
import { PessoaBackendMessages } from './pessoa-backend-message.service';
import { BaseService } from '../../base/base-service';
import { instanceToPlain, plainToInstance } from 'class-transformer';

@Injectable()
export class PessoaService extends BaseService<PessoaDTO, PessoaGridDTO> {
  private static readonly PESSOA = 'pessoa';

  constructor() {
    super(
      inject(HttpClient),
      inject(MessageService),
      inject(PessoaBackendMessages)
    );
  }

  getDomain(): string {
    return PessoaService.PESSOA;
  }

  protected override convertToDto(body: unknown): PessoaDTO {
    return plainToInstance(PessoaDTO, body as object) as PessoaDTO;
  }

  protected override convertToGrid(item: PessoaGridDTO): PessoaGridDTO {
    return plainToInstance(PessoaGridDTO, item as object) as PessoaGridDTO;
  }

  protected override convertToPlain(item: PessoaDTO): PessoaDTO {
    // Ensure it's a proper class instance using the factory method
    const instance = PessoaDTO.from(item);
    // Then convert to plain object with transformations applied
    return instanceToPlain(instance, {
      enableCircularCheck: true,
      exposeDefaultValues: true,
    }) as PessoaDTO;
  }
}
