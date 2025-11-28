import { Injectable } from "@angular/core";
import { AbstractBackendMessageService } from "../../base/services/backend-messsages/abstract-backend-message.service";

@Injectable({
  providedIn: 'root',
})
export class PerfilBackendMessages extends AbstractBackendMessageService {
  messages(): Record<string, string> {
    return {
      'perfil.nome.notBlank': $localize`:@@perfil.nome.notBlank:O nome do perfil não pode ser em branco.`,
    };
  }
}
