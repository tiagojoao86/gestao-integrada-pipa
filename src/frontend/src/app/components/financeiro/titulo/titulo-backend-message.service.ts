import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({
  providedIn: 'root',
})
export class TituloBackendMessages extends AbstractBackendMessageService {
  messages(): Record<string, string> {
    return {
      'titulo.tipo.notBlank': $localize`:@@titulo.tipo.notBlank:O tipo é obrigatório.`,
      'titulo.descricao.notBlank': $localize`:@@titulo.descricao.notBlank:A descrição é obrigatória.`,
      'titulo.pessoaId.notNull': $localize`:@@titulo.pessoaId.notNull:A pessoa é obrigatória.`,
      'titulo.planoContasId.notNull': $localize`:@@titulo.planoContasId.notNull:O plano de contas é obrigatório.`,
      'titulo.valorOriginal.notNull': $localize`:@@titulo.valorOriginal.notNull:O valor original é obrigatório.`,
      'titulo.dataEmissao.notNull': $localize`:@@titulo.dataEmissao.notNull:A data de emissão é obrigatória.`,
      'titulo.dataVencimento.notNull': $localize`:@@titulo.dataVencimento.notNull:A data de vencimento é obrigatória.`,
    };
  }
}
