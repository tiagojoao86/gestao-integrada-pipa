import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { DialogConfig, DialogType, DialogResult } from './dialog.model';

@Injectable()
export class DialogService {
  public dialogSubject = new Subject<DialogConfig | null>();
  private resultSubject = new Subject<DialogResult>();


  /**
   * Exibe um diálogo com apenas o botão OK
   * @param title Título do diálogo
   * @param message Mensagem do diálogo
   * @param okLabel Label customizada para o botão OK (opcional)
   * @returns Observable que emite quando o usuário clicar em OK
   */
  showOk(
    title: string,
    message: string,
    okLabel = 'OK'
  ): Observable<DialogResult> {
    const config: DialogConfig = {
      title,
      message,
      type: DialogType.OK,
      okLabel,
    };
    this.dialogSubject.next(config);
    return this.resultSubject.asObservable();
  }

  /**
   * Exibe um diálogo com botões Sim/Não
   * @param title Título do diálogo
   * @param message Mensagem do diálogo
   * @param yesLabel Label customizada para o botão Sim (opcional)
   * @param noLabel Label customizada para o botão Não (opcional)
   * @returns Observable que emite YES ou NO conforme a escolha do usuário
   */
  showYesNo(
    title: string,
    message: string,
    yesLabel = 'Sim',
    noLabel = 'Não'
  ): Observable<DialogResult> {
    const config: DialogConfig = {
      title,
      message,
      type: DialogType.YES_NO,
      yesLabel,
      noLabel,
    };
    this.dialogSubject.next(config);
    return this.resultSubject.asObservable();
  }

  /**
   * Exibe um diálogo com botões Sim/Não/Cancelar
   * @param title Título do diálogo
   * @param message Mensagem do diálogo
   * @param yesLabel Label customizada para o botão Sim (opcional)
   * @param noLabel Label customizada para o botão Não (opcional)
   * @param cancelLabel Label customizada para o botão Cancelar (opcional)
   * @returns Observable que emite YES, NO ou CANCEL conforme a escolha do usuário
   */
  showYesNoCancel(
    title: string,
    message: string,
    yesLabel = 'Sim',
    noLabel = 'Não',
    cancelLabel = 'Cancelar'
  ): Observable<DialogResult> {
    const config: DialogConfig = {
      title,
      message,
      type: DialogType.YES_NO_CANCEL,
      yesLabel,
      noLabel,
      cancelLabel,
    };
    this.dialogSubject.next(config);
    return this.resultSubject.asObservable();
  }

  /**
   * Fecha o diálogo e emite o resultado para os subscribers
   * @param result Resultado da ação do usuário
   */
  close(result: DialogResult): void {
    this.resultSubject.next(result);
    this.dialogSubject.next(null);
  }

  /**
   * Fecha o diálogo sem emitir resultado (usado internamente)
   */
  dismiss(): void {
    this.dialogSubject.next(null);
  }
}
