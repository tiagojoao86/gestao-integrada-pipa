import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({ providedIn: 'root' })
export class CondicaoPagamentoBackendMessageService extends AbstractBackendMessageService {
  protected entityMessages(): Record<string, string> {
    return {
      'condicaoPagamento.condicao.notBlank': $localize`:@@condicaoPagamento.condicao.notBlank:A condição de pagamento é obrigatória.`,
      'condicaoPagamento.condicao.unique': $localize`:@@condicaoPagamento.condicao.unique:Esta condição já está cadastrada.`,
      'condicaoPagamento.condicao.invalid': $localize`:@@condicaoPagamento.condicao.invalid:Formato de condição inválido. Use 'Nx' (ex: 3x) ou 'dias/dias/dias' (ex: 10/20/40).`,
      'condicaoPagamento.descricao.maxLength': $localize`:@@condicaoPagamento.descricao.maxLength:A descrição deve ter no máximo 400 caracteres.`,
    };
  }
}
