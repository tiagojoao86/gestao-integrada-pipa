import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({
  providedIn: 'root',
})
export class UnidadeNegocioBackendMessages extends AbstractBackendMessageService {
  protected entityMessages(): Record<string, string> {
    return {
      'unidadeNegocio.codigo.notBlank': $localize`:@@unidadeNegocio.codigo.notBlank:O código não pode ser em branco.`,
      'unidadeNegocio.codigo.maxLength': $localize`:@@unidadeNegocio.codigo.maxLength:O código deve ter no máximo 20 caracteres.`,
      'unidadeNegocio.codigo.unique': $localize`:@@unidadeNegocio.codigo.unique:Este código já está cadastrado.`,
      'unidadeNegocio.nome.notBlank': $localize`:@@unidadeNegocio.nome.notBlank:O nome não pode ser em branco.`,
      'unidadeNegocio.nome.maxLength': $localize`:@@unidadeNegocio.nome.maxLength:O nome deve ter no máximo 200 caracteres.`,
      'unidadeNegocio.cnpj.invalid': $localize`:@@unidadeNegocio.cnpj.invalid:CNPJ inválido.`,
      'unidadeNegocio.cnpj.maxLength': $localize`:@@unidadeNegocio.cnpj.maxLength:O CNPJ deve ter no máximo 18 caracteres.`,
      'unidadeNegocio.ativa.notNull': $localize`:@@unidadeNegocio.ativa.notNull:O campo ativa não pode ser nulo.`,
    };
  }
}
