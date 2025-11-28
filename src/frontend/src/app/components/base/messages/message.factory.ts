import { MessageService } from './messages.service';

export class MessageServiceFactory {
  private static instance: MessageService;

  public static create() {
    if (!this.instance) this.instance = new MessageService();

    return this.instance;
  }
}

export const messageServiceProvider = () => {
  return MessageServiceFactory.create();
};
