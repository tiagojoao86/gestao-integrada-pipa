import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({ providedIn: 'root' })
export class CategoriaTituloBackendMessageService extends AbstractBackendMessageService {
  messages(): Record<string, string> {
    return {
      'categoriaTitulo.nome.notBlank': $localize`:@@categoriaTitulo.nome.notBlank:Nome da categoria é obrigatório.`,
    };
  }
}
