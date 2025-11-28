import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable()
export class MessageService {
  public stateSubject = new Subject<Message>();

  mostrarMensagem(message: Message) {
    this.stateSubject.next(message);
  }

  info(message: string | string[]) {
    this.mostrarMensagem({
      message: message,
      messageType: MessageType.INFO,
    });
  }

  sucesso(message: string | string[]) {
    this.mostrarMensagem({
      message: message,
      messageType: MessageType.SUCCESS,
    });
  }

  alerta(message: string | string[]) {
    this.mostrarMensagem({
      message: message,
      messageType: MessageType.WARNING,
    });
  }

  erro(message: string | string[]) {
    this.mostrarMensagem({
      message: message,
      messageType: MessageType.ERROR,
    });
  }
}

export interface Message {
  message: string | string[];
  messageType: MessageType;
}

export enum MessageType {
  INFO,
  SUCCESS,
  ERROR,
  WARNING,
}
