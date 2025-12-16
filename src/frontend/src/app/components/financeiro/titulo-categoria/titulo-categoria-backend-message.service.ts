import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({ providedIn: 'root' })
export class TituloCategoriaBackendMessageService extends AbstractBackendMessageService {
  messages(): Record<string, string> {
    return {
      'tituloCategoria.nome.notBlank': $localize`:@@tituloCategoria.nome.notBlank:Nome da categoria é obrigatório.`,
    };
  }
}
