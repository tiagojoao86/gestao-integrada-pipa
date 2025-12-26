import { Injectable } from '@angular/core';
import { AbstractBackendMessageService } from '../../base/services/backend-messsages/abstract-backend-message.service';

@Injectable({
  providedIn: 'root',
})
export class PessoaBackendMessages extends AbstractBackendMessageService {
  protected entityMessages(): Record<string, string> {
    return {
      'pessoa.nome.notBlank': $localize`:@@pessoa.nome.notBlank:O nome da pessoa não pode ser em branco.`,
      'pessoa.email.invalid': $localize`:@@pessoa.email.invalid:Email inválido.`,
      'pessoa.telefone.invalid': $localize`:@@pessoa.telefone.invalid:Telefone inválido.`,
      'pessoa.ativa.notNull': $localize`:@@pessoa.ativa.notNull:O campo ativa não pode ser nulo.`,
      'pessoaFisica.cpf.notBlank': $localize`:@@pessoaFisica.cpf.notBlank:O CPF não pode ser em branco.`,
      'pessoaFisica.cpf.invalid': $localize`:@@pessoaFisica.cpf.invalid:CPF inválido.`,
      'pessoaFisica.cpf.unique': $localize`:@@pessoaFisica.cpf.unique:Este CPF já está cadastrado.`,
      'pessoaFisica.dataNascimento.notNull': $localize`:@@pessoaFisica.dataNascimento.notNull:A data de nascimento não pode ser nula.`,
      'pessoaFisica.dataNascimento.past': $localize`:@@pessoaFisica.dataNascimento.past:A data de nascimento deve ser no passado.`,
      'pessoaJuridica.cnpj.notBlank': $localize`:@@pessoaJuridica.cnpj.notBlank:O CNPJ não pode ser em branco.`,
      'pessoaJuridica.cnpj.invalid': $localize`:@@pessoaJuridica.cnpj.invalid:CNPJ inválido.`,
      'pessoaJuridica.cnpj.unique': $localize`:@@pessoaJuridica.cnpj.unique:Este CNPJ já está cadastrado.`,
      'pessoaJuridica.razaoSocial.notBlank': $localize`:@@pessoaJuridica.razaoSocial.notBlank:A razão social não pode ser em branco.`,
      'pessoaJuridica.nomeFantasia.notBlank': $localize`:@@pessoaJuridica.nomeFantasia.notBlank:O nome fantasia não pode ser em branco.`,
    };
  }
}
