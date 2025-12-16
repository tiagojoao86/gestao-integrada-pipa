import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({ providedIn: 'root' })
export class CentroCustoBackendMessageService extends AbstractBackendMessageService {
  messages(): Record<string, string> {
    return {
      'centroCusto.nome.notBlank': $localize`:@@centroCusto.nome.notBlank:O nome do centro de custo é obrigatório.`,
      'centroCusto.nome.maxLength': $localize`:@@centroCusto.nome.maxLength:O nome deve ter no máximo 200 caracteres.`,
      'centroCusto.unidadeNegocio.required': $localize`:@@centroCusto.unidadeNegocio.required:Unidade de negócio é obrigatória.`,
    };
  }
}
