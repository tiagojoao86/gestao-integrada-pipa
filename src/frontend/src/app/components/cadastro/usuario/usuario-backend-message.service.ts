import { Injectable } from "@angular/core";
import { AbstractBackendMessageService } from "../../base/services/backend-messsages/abstract-backend-message.service";

@Injectable({
  providedIn: 'root',
})
export class UsuarioBackendMessages extends AbstractBackendMessageService {
  messages(): Record<string, string> {
    return {
      'usuario.nome.notBlank': $localize`:@@usuario.nome.notBlank:O nome do usuário não pode ser em branco.`,
      'usuario.login.notBlank': $localize`:@@usuario.login.notBlank:O login não pode ser em branco.`,
      'usuario.senha.notBlank': $localize`:@@usuario.senha.notBlank:A senha não pode ser em branco.`,
      'usuario.login.unique': $localize`:@@usuario.login.unique:Este login já está em uso.`,
    };
  }
}