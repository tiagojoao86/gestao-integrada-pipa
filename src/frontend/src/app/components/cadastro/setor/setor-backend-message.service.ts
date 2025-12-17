import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({ providedIn: 'root' })
export class SetorBackendMessageService extends AbstractBackendMessageService {
  messages(): Record<string, string> {
    return {
      'setor.nome.notBlank': $localize`:@@setor.nome.notBlank:O nome do setor é obrigatório.`,
      'setor.nome.maxLength': $localize`:@@setor.nome.maxLength:O nome deve ter no máximo 200 caracteres.`,
      'setor.descricao.maxLength': $localize`:@@setor.descricao.maxLength:A descrição deve ter no máximo 500 caracteres.`,
      'setor.centroCusto.required': $localize`:@@setor.centroCusto.required:Centro de custo é obrigatório.`,
    };
  }
}
