import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({
  providedIn: 'root',
})
export class PlanoContasBackendMessages extends AbstractBackendMessageService {
  protected entityMessages(): Record<string, string> {
    return {
      'planoContas.codigo.notBlank': $localize`:@@planoContas.codigo.notBlank:O código não pode ser em branco.`,
      'planoContas.codigo.maxLength': $localize`:@@planoContas.codigo.maxLength:O código deve ter no máximo 20 caracteres.`,
      'planoContas.codigo.unique': $localize`:@@planoContas.codigo.unique:Este código já está cadastrado.`,
      'planoContas.descricao.notBlank': $localize`:@@planoContas.descricao.notBlank:A descrição não pode ser em branco.`,
      'planoContas.descricao.maxLength': $localize`:@@planoContas.descricao.maxLength:A descrição deve ter no máximo 200 caracteres.`,
      'planoContas.tipo.notNull': $localize`:@@planoContas.tipo.notNull:O tipo é obrigatório.`,
      'planoContas.planoPai.tipoInvalido': $localize`:@@planoContas.planoPai.tipoInvalido:Plano pai deve ser do mesmo tipo.`,
    };
  }
}
