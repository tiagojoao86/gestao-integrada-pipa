import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export abstract class AbstractBackendMessageService {

  getMessage(code: string): string {
    return this.messages()[code] || code;
  }

  getMessages(codes: string[]): string[] {
    return codes.map((code) => this.getMessage(code));
  }

  abstract messages(): Record<string, string>;

}