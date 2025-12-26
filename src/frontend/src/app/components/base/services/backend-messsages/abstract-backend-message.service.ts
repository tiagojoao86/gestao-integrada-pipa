import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export abstract class AbstractBackendMessageService {

  private static globalMessages(): Record<string, string> {
    return {
      'errors.resourceNotFound': $localize`:@@errors.resourceNotFound:Recurso não encontrado.`,
      'errors.invalidData': $localize`:@@errors.invalidData:Dados inválidos.`,
      'errors.internalServerError': $localize`:@@errors.internalServerError:Erro interno do servidor.`,
      'errors.badCredential': $localize`:@@errors.badCredential:Credenciais inválidas.`,
      'errors.notAuthorized': $localize`:@@errors.notAuthorized:Você não tem permissão para realizar esta ação.`,
      'errors.deletedEntity': $localize`:@@errors.deletedEntity:Não é possível alterar um registro que foi excluído.`,
    };
  }

  getMessage(code: string): string {
    // Primeiro tenta buscar nas mensagens específicas da entidade, depois nas globais
    return this.entityMessages()[code] || AbstractBackendMessageService.globalMessages()[code] || code;
  }

  getMessages(codes: string[]): string[] {
    return codes.map((code) => this.getMessage(code));
  }

  // Método que retorna mensagens específicas da entidade (subclasses devem implementar)
  protected abstract entityMessages(): Record<string, string>;

  // Método messages() agora retorna a combinação de mensagens globais + específicas
  messages(): Record<string, string> {
    return {
      ...AbstractBackendMessageService.globalMessages(),
      ...this.entityMessages(),
    };
  }

}