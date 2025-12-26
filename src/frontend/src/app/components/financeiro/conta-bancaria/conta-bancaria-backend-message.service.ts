import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({
  providedIn: 'root',
})
export class ContaBancariaBackendMessages extends AbstractBackendMessageService {
  protected entityMessages(): Record<string, string> {
    return {
      'contaBancaria.nome.notBlank': $localize`:@@contaBancaria.nome.notBlank:O nome da conta não pode ser em branco.`,
      'contaBancaria.nome.maxLength': $localize`:@@contaBancaria.nome.maxLength:O nome deve ter no máximo 100 caracteres.`,
      'contaBancaria.tipo.notNull': $localize`:@@contaBancaria.tipo.notNull:O tipo da conta é obrigatório.`,
      'contaBancaria.saldoInicial.invalid': $localize`:@@contaBancaria.saldoInicial.invalid:Saldo inicial inválido.`,
    };
  }
}
